package com.conectaai.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto(
        UserResponseDto user,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Integer expiresIn
) {
    public AuthResponseDto(UserResponseDto user, String accessToken, String refreshToken, Integer expiresIn) {
        this(user, accessToken, refreshToken, "Bearer", expiresIn);
    }
}

