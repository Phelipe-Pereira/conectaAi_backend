package com.conectaai.messaging;

import com.conectaai.enums.Provider;

import java.time.OffsetDateTime;

public record WebhookMessage(
        Provider provider,
        String targetType,
        String eventType,
        String eventId,
        OffsetDateTime occurredAt,
        String externalId,
        String providerEntityId,
        String payload
) {
}


