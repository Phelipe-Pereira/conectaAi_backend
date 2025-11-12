package com.conectaai.dto.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record WebhookEndpointResponseDto(
        Long id,

        Provider provider,

        @JsonProperty("provider_endpoint_id")
        String providerEndpointId,

        String url,

        Boolean active,

        @JsonProperty("enabled_events")
        List<String> enabledEvents,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}

