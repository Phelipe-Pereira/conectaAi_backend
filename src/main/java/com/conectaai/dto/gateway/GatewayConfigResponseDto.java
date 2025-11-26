package com.conectaai.dto.gateway;

public record GatewayConfigResponseDto(
        Boolean hasAsaasApiKey,
        Boolean asaasApiKeyValid
) {
}

