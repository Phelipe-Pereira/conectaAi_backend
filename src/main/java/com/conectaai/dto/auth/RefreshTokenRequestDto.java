package com.conectaai.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {
}

