package com.conectaai.service.webhook;

import com.conectaai.config.gateway.GatewayConfig;
import com.conectaai.enums.Provider;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class WebhookSignatureValidator {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookSignatureValidator.class);
    private static final String METHOD_VALIDATE_STRIPE = "validateStripe";
    private static final String METHOD_VALIDATE_MERCADO_PAGO = "validateMercadoPago";
    private static final String METHOD_VALIDATE_ASAAS = "validateAsaas";

    private final GatewayConfig gatewayConfig;
    private final WebhookEndpointRepository webhookEndpointRepository;

    @Value("${webhooks.validation.enabled:true}")
    private boolean validationEnabled;

    @Value("${webhooks.asaas.defaultAuthHeader:X-Webhook-Auth}")
    private String asaasDefaultAuthHeader;

    public boolean isValid(Provider provider, HttpHeaders headers, String rawPayload, String webhookEndpointId) {
        if (!validationEnabled) {
            LOGGER.info("isValid", "Validação de webhook desabilitada para provider={}", provider);
            return true;
        }

        return switch (provider) {
            case STRIPE -> validateStripe(headers, rawPayload, webhookEndpointId);
            case MERCADO_PAGO -> validateMercadoPago(headers, rawPayload, webhookEndpointId);
            case ASAAS -> validateAsaas(headers, webhookEndpointId);
        };
    }

    private boolean validateStripe(HttpHeaders headers, String rawPayload, String webhookEndpointId) {
        String signature = headers.getFirst("Stripe-Signature");
        if (signature == null || signature.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_STRIPE, "Stripe-Signature header ausente");
            return false;
        }

        String secret = getStripeSecret(webhookEndpointId);
        if (secret == null || secret.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_STRIPE, "Stripe secret não configurado");
            return false;
        }

        try {
            com.stripe.net.Webhook.Signature.verifyHeader(
                    rawPayload,
                    signature,
                    secret,
                    300
            );
            return true;
        } catch (com.stripe.exception.SignatureVerificationException e) {
            LOGGER.warn(METHOD_VALIDATE_STRIPE, "Assinatura Stripe inválida: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error(METHOD_VALIDATE_STRIPE, "Erro ao validar assinatura Stripe", e);
            return false;
        }
    }

    private boolean validateMercadoPago(HttpHeaders headers, String rawPayload, String webhookEndpointId) {
        String signature = extractMercadoPagoSignature(headers);
        if (signature == null || signature.isBlank()) {
            return false;
        }

        String secret = getMercadoPagoSecret(webhookEndpointId);
        if (secret == null || secret.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_MERCADO_PAGO, "Mercado Pago secret não configurado");
            return false;
        }

        try {
            String calculatedSignature = calculateMercadoPagoSignature(headers, rawPayload, secret);
            boolean isValid = MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8),
                    calculatedSignature.getBytes(StandardCharsets.UTF_8)
            );

            if (!isValid) {
                LOGGER.warn(METHOD_VALIDATE_MERCADO_PAGO, "Assinatura Mercado Pago inválida");
            }
            return isValid;
        } catch (Exception e) {
            LOGGER.error(METHOD_VALIDATE_MERCADO_PAGO, "Erro ao validar assinatura Mercado Pago", e);
            return false;
        }
    }

    private String extractMercadoPagoSignature(HttpHeaders headers) {
        String signature = headers.getFirst("x-signature");
        if (signature == null || signature.isBlank()) {
            signature = headers.getFirst("X-Signature");
        }
        if (signature == null || signature.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_MERCADO_PAGO, "x-signature header ausente");
            return null;
        }
        return signature;
    }

    private String calculateMercadoPagoSignature(HttpHeaders headers, String rawPayload, String secret) 
            throws Exception {
        String payloadToSign = buildMercadoPagoPayloadToSign(headers, rawPayload);
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(payloadToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String buildMercadoPagoPayloadToSign(HttpHeaders headers, String rawPayload) {
        String xRequestId = headers.getFirst("x-request-id");
        String dataId = headers.getFirst("x-data-id");
        String dataIdStr = (dataId != null ? dataId : "") + (xRequestId != null ? xRequestId : "");
        return dataIdStr + rawPayload;
    }

    private boolean validateAsaas(HttpHeaders headers, String webhookEndpointId) {
        String receivedToken = extractAsaasToken(headers);
        if (receivedToken == null || receivedToken.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_ASAAS, "Token de autenticação Asaas ausente nos headers");
            return false;
        }

        String expectedToken = getAsaasAuthToken(webhookEndpointId);
        if (expectedToken == null || expectedToken.isBlank()) {
            LOGGER.warn(METHOD_VALIDATE_ASAAS, "Token de autenticação Asaas não configurado");
            return false;
        }

        boolean isValid = MessageDigest.isEqual(
                receivedToken.getBytes(StandardCharsets.UTF_8),
                expectedToken.getBytes(StandardCharsets.UTF_8)
        );

        if (!isValid) {
            LOGGER.warn(METHOD_VALIDATE_ASAAS, "Token de autenticação Asaas inválido");
        }
        return isValid;
    }

    private String extractAsaasToken(HttpHeaders headers) {
        String token = headers.getFirst(asaasDefaultAuthHeader);
        if (token != null && !token.isBlank()) {
            return token;
        }
        token = headers.getFirst("X-Webhook-Auth");
        if (token != null && !token.isBlank()) {
            return token;
        }
        token = headers.getFirst("X-Auth-Token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        String authorization = headers.getFirst("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            if (authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
            return authorization;
        }
        return null;
    }

    private String getStripeSecret(String webhookEndpointId) {
        if (webhookEndpointId != null && !webhookEndpointId.isBlank()) {
            try {
                return webhookEndpointRepository.findById(Long.parseLong(webhookEndpointId))
                        .map(endpoint -> endpoint.getSecret())
                        .orElse(gatewayConfig.getStripeWebhookSecret());
            } catch (NumberFormatException e) {
                return gatewayConfig.getStripeWebhookSecret();
            }
        }
        return gatewayConfig.getStripeWebhookSecret();
    }

    private String getMercadoPagoSecret(String webhookEndpointId) {
        if (webhookEndpointId != null && !webhookEndpointId.isBlank()) {
            try {
                return webhookEndpointRepository.findById(Long.parseLong(webhookEndpointId))
                        .map(endpoint -> endpoint.getSecret())
                        .orElse(gatewayConfig.getMpWebhookSecret());
            } catch (NumberFormatException e) {
                return gatewayConfig.getMpWebhookSecret();
            }
        }
        return gatewayConfig.getMpWebhookSecret();
    }

    private String getAsaasAuthToken(String webhookEndpointId) {
        if (webhookEndpointId != null && !webhookEndpointId.isBlank()) {
            try {
                return webhookEndpointRepository.findById(Long.parseLong(webhookEndpointId))
                        .map(endpoint -> endpoint.getAuthToken() != null 
                                ? endpoint.getAuthToken() 
                                : endpoint.getSecret())
                        .orElse(getDefaultAsaasToken());
            } catch (NumberFormatException e) {
                return getDefaultAsaasToken();
            }
        }
        return getDefaultAsaasToken();
    }

    private String getDefaultAsaasToken() {
        if (gatewayConfig.getAsaasWebhookAuthToken() != null && !gatewayConfig.getAsaasWebhookAuthToken().isBlank()) {
            return gatewayConfig.getAsaasWebhookAuthToken();
        }
        return gatewayConfig.getAsaasWebhookSecret();
    }
}

