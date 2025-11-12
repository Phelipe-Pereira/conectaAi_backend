package com.conectaai.adapter.gateway;

import java.util.Map;

public record GatewayWebhookResponse(
        String providerEndpointId,
        String url,
        String secret,
        Map<String, Object> metadata
) {
}

