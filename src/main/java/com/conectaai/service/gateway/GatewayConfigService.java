package com.conectaai.service.gateway;

import com.conectaai.domain.User;
import com.conectaai.exception.UserNotFoundException;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GatewayConfigService {

    private static final AppLogger LOGGER = AppLogger.getLogger(GatewayConfigService.class);

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final AsaasSdkFactory asaasSdkFactory;

    @Transactional
    public void saveAsaasApiKey(Long userId, String apiKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        String encryptedKey = encryptionService.encrypt(apiKey);
        user.setAsaasApiKey(encryptedKey);
        userRepository.save(user);

        LOGGER.info("saveAsaasApiKey", "Chave do Asaas salva para userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public String getAsaasApiKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (user.getAsaasApiKey() == null || user.getAsaasApiKey().isBlank()) {
            return null;
        }

        try {
            return encryptionService.decrypt(user.getAsaasApiKey());
        } catch (Exception e) {
            LOGGER.error("getAsaasApiKey", "Erro ao descriptografar chave do Asaas para userId: {}", userId, e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasAsaasApiKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return user.getAsaasApiKey() != null && !user.getAsaasApiKey().isBlank();
    }

    @Transactional
    public boolean validateAsaasApiKey(String apiKey) {
        try {
            asaasSdkFactory.createSdkForUser(apiKey);
            return true;
        } catch (Exception e) {
            LOGGER.warn("validateAsaasApiKey", "Chave do Asaas inválida: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void removeAsaasApiKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        user.setAsaasApiKey(null);
        userRepository.save(user);

        LOGGER.info("removeAsaasApiKey", "Chave do Asaas removida para userId: {}", userId);
    }
}

