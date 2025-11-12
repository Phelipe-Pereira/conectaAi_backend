package com.conectaai.config.gateway;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GatewayConfig {

    @Value("${ASAAS_TOKEN:}")
    private String asaasToken;

    @Value("${STRIPE_SECRET:}")
    private String stripeSecret;

    @Value("${MP_ACCESS_TOKEN:}")
    private String mpAccessToken;

    @Value("${STRIPE_WEBHOOK_SECRET:${STRIPE_SECRET:}}")
    private String stripeWebhookSecret;

    @Value("${MP_WEBHOOK_SECRET:${MP_ACCESS_TOKEN:}}")
    private String mpWebhookSecret;

    @Value("${ASAAS_WEBHOOK_SECRET:${ASAAS_TOKEN:}}")
    private String asaasWebhookSecret;

    @Value("${ASAAS_WEBHOOK_AUTHTOKEN:}")
    private String asaasWebhookAuthToken;
}

