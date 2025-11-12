package com.conectaai.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WebhookEndpointUpdateDto(
        @Size(max = 500, message = "URL deve ter no máximo 500 caracteres")
        String url,

        @JsonProperty("enabled_events")
        List<@Size(max = 100) String> enabledEvents,

        Boolean active
) {
}

