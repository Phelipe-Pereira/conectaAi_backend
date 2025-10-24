package com.conectaai.dto.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record WebhookEventRequestDto(
        @NotNull(message = "Provider é obrigatório")
        Provider provider,
        
        @NotBlank(message = "Tipo do evento é obrigatório")
        @Size(max = 100, message = "Tipo do evento muito longo")
        @JsonProperty("event_type")
        String eventType,
        
        @NotBlank(message = "Payload é obrigatório")
        @Size(max = 10000, message = "Payload muito grande")
        String payload,
        
        @Size(max = 100, message = "ID externo muito longo")
        @JsonProperty("external_id")
        String externalId,
        
        @Size(max = 500, message = "Assinatura muito longa")
        String signature
) {
}

