package com.conectaai.dto.subscription;

import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponseDto(
        String id,
        
        @JsonProperty("public_id")
        String publicId,
        
        @JsonProperty("customer_id")
        String customerId,
        
        BigDecimal amount,
        
        Currency currency,
        
        SubscriptionInterval interval,
        
        SubscriptionStatus status,
        
        Provider provider,
        
        @JsonProperty("provider_reference")
        String providerReference,
        
        @JsonProperty("start_at")
        LocalDateTime startAt,
        
        @JsonProperty("end_at")
        LocalDateTime endAt,
        
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

