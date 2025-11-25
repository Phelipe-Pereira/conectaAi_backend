package com.conectaai.service.webhook;

import com.conectaai.domain.WebhookEvent;
import com.conectaai.enums.Provider;
import com.conectaai.logger.AppLogger;
import com.conectaai.messaging.WebhookMessage;
import com.conectaai.messaging.WebhookPublisher;
import com.conectaai.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookService.class);
    private static final String METHOD_HANDLE_PROVIDER_CALLBACK = "handleProviderCallback";

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookPublisher publisher;
    private final WebhookPayloadNormalizer normalizer;
    private final WebhookSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    public void handleProviderCallback(Provider provider, HttpHeaders headers, String rawPayload) {
        String eventType = extractEventType(provider, headers);
        
        if (eventType == null || "unknown".equals(eventType)) {
            eventType = extractEventTypeFromPayload(provider, rawPayload);
        }
        
        String webhookEndpointId = extractWebhookEndpointId(headers);

        if (!signatureValidator.isValid(provider, headers, rawPayload, webhookEndpointId)) {
            LOGGER.warn(METHOD_HANDLE_PROVIDER_CALLBACK,
                    "Assinatura inválida para provider {} e event {}", provider, eventType);
            String hash = Integer.toHexString(rawPayload.hashCode());
            String fallbackEventId = provider.name() + ":" + eventType + ":" + hash;
            persistEvent(provider, eventType, rawPayload, false, "invalid signature", fallbackEventId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assinatura inválida");
        }

        WebhookPayloadNormalizer.NormalizedWebhookData normalized = 
                normalizer.normalize(provider, eventType, rawPayload);

        if (isDuplicate(provider, normalized.eventId())) {
            LOGGER.info(METHOD_HANDLE_PROVIDER_CALLBACK,
                    "Evento duplicado ignorado: provider={} eventId={}", 
                    provider, normalized.eventId());
            return;
        }

        WebhookEvent saved = persistEvent(provider, normalized.eventType(), rawPayload, false, null, normalized.eventId());

        try {
            WebhookMessage message = buildMessage(provider, normalized);
            publisher.publish(message, normalized.targetType());
            markProcessed(saved);
            LOGGER.info(METHOD_HANDLE_PROVIDER_CALLBACK,
                    "Webhook processado e publicado: provider={} eventId={} targetType={}",
                    provider, normalized.eventId(), normalized.targetType());
        } catch (Exception e) {
            LOGGER.error(METHOD_HANDLE_PROVIDER_CALLBACK,
                    "Erro ao publicar webhook: provider={} eventId={}", provider, normalized.eventId(), e);
            markFailed(saved, "Erro ao publicar: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar webhook");
        }
    }

    private WebhookMessage buildMessage(Provider provider, WebhookPayloadNormalizer.NormalizedWebhookData normalized) {
        return new WebhookMessage(
                provider,
                normalized.targetType(),
                normalized.eventType(),
                normalized.eventId(),
                normalized.occurredAt(),
                normalized.externalId(),
                normalized.providerEntityId(),
                normalized.rawPayload()
        );
    }

    private boolean isDuplicate(Provider provider, String eventId) {
        return webhookEventRepository.existsByProviderAndExternalId(provider, eventId);
    }

    private WebhookEvent persistEvent(Provider provider, String eventType, String payload,
                                      boolean processed, String error, String eventId) {
        WebhookEvent e = WebhookEvent.builder()
                .provider(provider)
                .eventType(eventType)
                .payload(payload)
                .processed(processed)
                .errorMessage(error)
                .externalId(eventId)
                .build();
        return webhookEventRepository.save(e);
    }

    private void markProcessed(WebhookEvent e) {
        e.setProcessed(true);
        e.setProcessedAt(LocalDateTime.now());
        webhookEventRepository.save(e);
    }

    private void markFailed(WebhookEvent e, String error) {
        e.setProcessed(false);
        e.setErrorMessage(error);
        webhookEventRepository.save(e);
    }

    private String extractEventType(Provider provider, HttpHeaders headers) {
        if (provider == Provider.STRIPE) {
            String type = headers.getFirst("Stripe-Event-Type");
            if (type != null) {
                return type;
            }
        }
        if (provider == Provider.ASAAS) {
            String type = headers.getFirst("asaas-event");
            if (type != null) {
                return type;
            }
        }
        if (provider == Provider.MERCADO_PAGO) {
            String type = headers.getFirst("x-topic");
            if (type != null) {
                return type;
            }
        }
        String type = headers.getFirst("x-event-type");
        if (type != null) {
            return type;
        }
        return "unknown";
    }

    private String extractEventTypeFromPayload(Provider provider, String rawPayload) {
        try {
            JsonNode payload = objectMapper.readTree(rawPayload);
            
            if (provider == Provider.ASAAS) {
                String event = payload.path("event").asText(null);
                if (event != null && !event.isBlank()) {
                    LOGGER.info("extractEventTypeFromPayload", "Evento extraído do payload do Asaas: {}", event);
                    return event;
                }
            }
            
            return "unknown";
        } catch (Exception e) {
            LOGGER.warn("extractEventTypeFromPayload", "Erro ao extrair tipo de evento do payload: {}", e.getMessage());
            return "unknown";
        }
    }

    private String extractWebhookEndpointId(HttpHeaders headers) {
        return headers.getFirst("X-Webhook-Endpoint-Id");
    }
}


