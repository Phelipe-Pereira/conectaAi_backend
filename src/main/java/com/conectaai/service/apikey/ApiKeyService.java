package com.conectaai.service.apikey;

import com.conectaai.domain.ApiKey;
import com.conectaai.domain.User;
import com.conectaai.dto.apikey.ApiKeyCreateResponseDto;
import com.conectaai.dto.apikey.ApiKeyRequestDto;
import com.conectaai.dto.apikey.ApiKeyResponseDto;
import com.conectaai.exception.ApiKeyNotFoundException;
import com.conectaai.exception.UnauthorizedApiKeyAccessException;
import com.conectaai.exception.UserNotFoundException;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.ApiKeyRepository;
import com.conectaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final AppLogger LOGGER = AppLogger.getLogger(ApiKeyService.class);
    private static final int API_KEY_BYTES = 48;
    private static final int PREFIX_LENGTH = 8;
    private static final String API_KEY_PREFIX = "ck_";

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiKeyCreateResponseDto createApiKey(Long userId, ApiKeyRequestDto request) {
        LOGGER.info("createApiKey", "Criando API Key para userId: {}, nome: {}", userId, request.name());

        User user = findUserById(userId);
        String fullApiKey = generateApiKey();
        String keyHash = passwordEncoder.encode(fullApiKey);
        String keyPrefix = fullApiKey.substring(0, API_KEY_PREFIX.length() + PREFIX_LENGTH);

        ApiKey apiKey = ApiKey.builder()
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .name(request.name())
                .description(request.description())
                .user(user)
                .active(true)
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        LOGGER.info("createApiKey", "API Key criada com sucesso. ID: {}, Prefix: {}", apiKey.getId(), keyPrefix);

        String warning = "⚠️ IMPORTANTE: Esta chave será exibida apenas uma vez. Guarde-a em local seguro.";

        return new ApiKeyCreateResponseDto(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getDescription(),
                fullApiKey,
                apiKey.getKeyPrefix(),
                apiKey.getActive(),
                apiKey.getExpiresAt(),
                apiKey.getCreatedAt(),
                warning
        );
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponseDto> getUserApiKeys(Long userId, Pageable pageable) {
        LOGGER.info("getUserApiKeys", "Listando API Keys para userId: {}", userId);
        User user = findUserById(userId);
        return apiKeyRepository.findByUser(user, pageable).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public ApiKeyResponseDto getApiKeyById(Long userId, Long apiKeyId) {
        LOGGER.info("getApiKeyById", "Buscando API Key ID: {} para userId: {}", apiKeyId, userId);
        ApiKey apiKey = findApiKeyById(apiKeyId);
        verifyApiKeyOwnership(apiKey, userId);
        return toResponseDto(apiKey);
    }

    @Transactional
    public void revokeApiKey(Long userId, Long apiKeyId) {
        LOGGER.info("revokeApiKey", "Revogando API Key ID: {} para userId: {}", apiKeyId, userId);
        ApiKey apiKey = findApiKeyById(apiKeyId);
        verifyApiKeyOwnership(apiKey, userId);
        apiKey.revoke();
        apiKeyRepository.save(apiKey);
        LOGGER.info("revokeApiKey", "API Key revogada com sucesso. ID: {}", apiKeyId);
    }

    @Transactional
    public void revokeAllUserApiKeys(Long userId) {
        LOGGER.info("revokeAllUserApiKeys", "Revogando todas as API Keys do userId: {}", userId);
        User user = findUserById(userId);
        List<ApiKey> activeKeys = apiKeyRepository.findByUserAndActiveTrue(user);
        activeKeys.forEach(ApiKey::revoke);
        apiKeyRepository.saveAll(activeKeys);
        LOGGER.info("revokeAllUserApiKeys", "Todas as API Keys revogadas. Total: {}", activeKeys.size());
    }

    @Transactional
    public User validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.startsWith(API_KEY_PREFIX) || apiKey.length() < API_KEY_PREFIX.length() + PREFIX_LENGTH) {
            LOGGER.warn("validateApiKey", "API Key com formato inválido");
            return null;
        }

        String prefix = apiKey.substring(0, API_KEY_PREFIX.length() + PREFIX_LENGTH);
        LOGGER.info("validateApiKey", "Validando API Key com prefixo: {}", prefix);

        List<ApiKey> keysWithPrefix = apiKeyRepository.findByKeyPrefix(prefix);
        if (keysWithPrefix.isEmpty()) {
            LOGGER.warn("validateApiKey", "Nenhuma API Key encontrada com prefixo: {}", prefix);
            return null;
        }

        for (ApiKey key : keysWithPrefix) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                if (!key.isValid()) {
                    LOGGER.warn("validateApiKey", "API Key inválida (expirada ou revogada). ID: {}", key.getId());
                    return null;
                }
                
                key.markAsUsed();
                apiKeyRepository.save(key);
                
                User user = userRepository.findByIdWithRoles(key.getUser().getId())
                        .orElse(null);
                
                if (user == null) {
                    LOGGER.warn("validateApiKey", "Usuário não encontrado para API Key. UserId: {}", key.getUser().getId());
                    return null;
                }
                
                LOGGER.info("validateApiKey", "API Key válida. UserId: {}, ApiKeyId: {}", 
                        user.getId(), key.getId());
                return user;
            }
        }

        LOGGER.warn("validateApiKey", "API Key não encontrada ou inválida");
        return null;
    }

    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[API_KEY_BYTES];
        random.nextBytes(bytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return API_KEY_PREFIX + base64;
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> {
                    LOGGER.error("findUserById", "Usuário não encontrado: {}", userId);
                    return new UserNotFoundException("Usuário não encontrado");
                });
    }

    private ApiKey findApiKeyById(Long apiKeyId) {
        return apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> {
                    LOGGER.error("findApiKeyById", "API Key não encontrada: {}", apiKeyId);
                    return new ApiKeyNotFoundException("API Key não encontrada");
                });
    }

    private void verifyApiKeyOwnership(ApiKey apiKey, Long userId) {
        if (!apiKey.getUser().getId().equals(userId)) {
            LOGGER.warn("verifyApiKeyOwnership", "Tentativa de acesso não autorizado. UserId: {}, ApiKeyId: {}", 
                    userId, apiKey.getId());
            throw new UnauthorizedApiKeyAccessException("Acesso não autorizado");
        }
    }

    private ApiKeyResponseDto toResponseDto(ApiKey apiKey) {
        return new ApiKeyResponseDto(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getDescription(),
                apiKey.getKeyPrefix(),
                apiKey.getActive(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getCreatedAt(),
                apiKey.getUpdatedAt()
        );
    }
}

