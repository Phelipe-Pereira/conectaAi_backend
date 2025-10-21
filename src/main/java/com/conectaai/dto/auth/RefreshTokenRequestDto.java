package com.conectaai.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token é obrigatório")
        @JsonProperty("refresh_token")
        String refreshToken
) {}

