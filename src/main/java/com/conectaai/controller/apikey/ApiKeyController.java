package com.conectaai.controller.apikey;

import com.conectaai.config.PaginationConfig;
import com.conectaai.dto.apikey.ApiKeyCreateResponseDto;
import com.conectaai.dto.apikey.ApiKeyRequestDto;
import com.conectaai.dto.apikey.ApiKeyResponseDto;
import com.conectaai.logger.AppLogger;
import com.conectaai.security.SecurityUtils;
import com.conectaai.service.apikey.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ApiKeyController {

    private static final AppLogger LOGGER = AppLogger.getLogger(ApiKeyController.class);

    private final ApiKeyService apiKeyService;
    private final PaginationConfig paginationConfig;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponseDto> createApiKey(@Valid @RequestBody ApiKeyRequestDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("createApiKey", "Criando API Key para userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.createApiKey(userId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ApiKeyResponseDto>> getUserApiKeys(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getUserApiKeys", "Listando API Keys para userId: {}", userId);
        Pageable pageable = createPageable(page, size, sort, direction);
        return ResponseEntity.ok(apiKeyService.getUserApiKeys(userId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponseDto> getApiKeyById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getApiKeyById", "Buscando API Key ID: {} para userId: {}", id, userId);
        return ResponseEntity.ok(apiKeyService.getApiKeyById(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("revokeApiKey", "Revogando API Key ID: {} para userId: {}", id, userId);
        apiKeyService.revokeApiKey(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> revokeAllApiKeys() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("revokeAllApiKeys", "Revogando todas as API Keys para userId: {}", userId);
        apiKeyService.revokeAllUserApiKeys(userId);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, Integer size, String sort, String direction) {
        int pageSize = (size != null && size > 0 && size <= paginationConfig.getMaxPageSize())
                ? size
                : paginationConfig.getDefaultPageSize();

        Sort.Direction sortDirection = parseDirection(direction);
        return PageRequest.of(page, pageSize, Sort.by(sortDirection, sort));
    }

    private Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.DESC;
        }
    }
}

