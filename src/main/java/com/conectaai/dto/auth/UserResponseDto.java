package com.conectaai.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDto(
        String id,
        String username,
        String email,
        Boolean active,
        List<String> roles,
        @JsonProperty("created_at")
        LocalDateTime createdAt
) { }


