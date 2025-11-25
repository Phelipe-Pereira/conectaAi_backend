package com.conectaai.dto.apikey;

import java.time.Instant;
import java.time.LocalDateTime;

public record ApiKeyCreateResponseDto(
        Long id,
        String name,
        String description,
        String apiKey,
        String keyPrefix,
        Boolean active,
        Instant expiresAt,
        LocalDateTime createdAt,
        String warning
) {
}

