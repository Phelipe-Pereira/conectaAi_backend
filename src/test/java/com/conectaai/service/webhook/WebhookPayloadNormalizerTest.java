package com.conectaai.service.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookPayloadNormalizerTest {

    private WebhookPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new WebhookPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeStripePaymentIntent() {
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_123\",\"metadata\":{\"external_id\":\"pay_abc\"}}},\"created\":1234567890}";
        
        WebhookPayloadNormalizer.NormalizedWebhookData result = normalizer.normalize(
                Provider.STRIPE, "payment_intent.succeeded", payload);

        assertNotNull(result);
        assertEquals("payment", result.targetType());
        assertEquals("payment_intent.succeeded", result.eventType());
        assertNotNull(result.eventId());
    }

    @Test
    void normalizeMercadoPagoPayment() {
        String payload = "{\"id\":123,\"action\":\"payment.updated\",\"data\":{\"id\":\"123\",\"external_reference\":\"pay_abc\"}}";
        
        WebhookPayloadNormalizer.NormalizedWebhookData result = normalizer.normalize(
                Provider.MERCADO_PAGO, "payment.updated", payload);

        assertNotNull(result);
        assertEquals("payment", result.targetType());
    }

    @Test
    void normalizeAsaasPayment() {
        String payload = "{\"event\":{\"id\":\"evt_123\",\"event\":\"PAYMENT_RECEIVED\"},\"payment\":{\"id\":\"pay_123\",\"externalReference\":\"pay_abc\"}}";
        
        WebhookPayloadNormalizer.NormalizedWebhookData result = normalizer.normalize(
                Provider.ASAAS, "PAYMENT_RECEIVED", payload);

        assertNotNull(result);
        assertEquals("payment", result.targetType());
    }
}

