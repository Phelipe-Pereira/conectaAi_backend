package com.conectaai.dto.apikey;

import java.time.Instant;
import java.time.LocalDateTime;

public record ApiKeyResponseDto(
        Long id,
        String name,
        String description,
        String keyPrefix,
        Boolean active,
        Instant expiresAt,
        Instant lastUsedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

