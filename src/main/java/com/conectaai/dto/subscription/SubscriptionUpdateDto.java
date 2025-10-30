package com.conectaai.dto.subscription;

import com.conectaai.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record SubscriptionUpdateDto(
        SubscriptionStatus status,

        @JsonProperty("provider_subscription_id")
        @Size(max = 100)
        String providerSubscriptionId,

        @JsonProperty("end_at")
        LocalDateTime endAt,

        @Size(max = 500)
        String description
) {
}

