package com.conectaai.dto.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record WebhookEventResponseDto(
        String id,
        
        @JsonProperty("webhook_endpoint_id")
        String webhookEndpointId,
        
        Provider provider,
        
        @JsonProperty("event_type")
        String eventType,
        
        Boolean processed,
        
        @JsonProperty("external_id")
        String externalId,
        
        @JsonProperty("processed_at")
        LocalDateTime processedAt,
        
        @JsonProperty("error_message")
        String errorMessage,
        
        @JsonProperty("retry_count")
        Integer retryCount,
        
        @JsonProperty("received_at")
        LocalDateTime receivedAt
) {
}

