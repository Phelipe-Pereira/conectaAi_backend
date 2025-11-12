package com.conectaai.dto.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WebhookEndpointRequestDto(
        @NotNull(message = "Provider é obrigatório")
        Provider provider,

        @NotBlank(message = "URL é obrigatória")
        @Size(max = 500, message = "URL deve ter no máximo 500 caracteres")
        String url,

        @NotEmpty(message = "Eventos habilitados são obrigatórios")
        @JsonProperty("enabled_events")
        List<@NotBlank @Size(max = 100) String> enabledEvents,

        @Size(max = 100, message = "Secret deve ter no máximo 100 caracteres")
        String secret
) {
}

