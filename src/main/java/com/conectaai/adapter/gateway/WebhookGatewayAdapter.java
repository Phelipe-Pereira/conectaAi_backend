package com.conectaai.adapter.gateway;

import java.util.List;

public interface WebhookGatewayAdapter {

    GatewayWebhookResponse createWebhook(String url, List<String> events, String secret);

    GatewayWebhookResponse getWebhook(String providerEndpointId);

    GatewayWebhookResponse updateWebhook(String providerEndpointId, String url, List<String> events);

    void deleteWebhook(String providerEndpointId);
}

