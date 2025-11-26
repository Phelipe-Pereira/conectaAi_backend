package com.conectaai.dto.gateway;

import jakarta.validation.constraints.NotBlank;

public record GatewayConfigRequestDto(
        @NotBlank(message = "Chave do Asaas é obrigatória")
        String asaasApiKey
) {
}

