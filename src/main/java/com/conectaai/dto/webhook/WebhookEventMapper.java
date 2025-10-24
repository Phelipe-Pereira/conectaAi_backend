package com.conectaai.dto.webhook;

import com.conectaai.domain.WebhookEndpoint;
import com.conectaai.domain.WebhookEvent;

public class WebhookEventMapper {

    private WebhookEventMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static WebhookEvent toEntity(WebhookEventRequestDto webhookRequest, WebhookEndpoint webhookEndpoint) {
        return WebhookEvent.builder()
                .webhookEndpoint(webhookEndpoint)
                .provider(webhookRequest.provider())
                .eventType(webhookRequest.eventType())
                .payload(webhookRequest.payload())
                .externalId(webhookRequest.externalId())
                .signature(webhookRequest.signature())
                .processed(false)
                .retryCount(0)
                .build();
    }

    public static WebhookEventResponseDto toResponseDto(WebhookEvent webhookEvent) {
        return new WebhookEventResponseDto(
                webhookEvent.getId().toString(),
                webhookEvent.getWebhookEndpoint().getId().toString(),
                webhookEvent.getProvider(),
                webhookEvent.getEventType(),
                webhookEvent.getProcessed(),
                webhookEvent.getExternalId(),
                webhookEvent.getProcessedAt(),
                webhookEvent.getErrorMessage(),
                webhookEvent.getRetryCount(),
                webhookEvent.getReceivedAt()
        );
    }
}

