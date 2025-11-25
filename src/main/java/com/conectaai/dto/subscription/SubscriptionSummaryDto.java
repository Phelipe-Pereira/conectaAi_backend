package com.conectaai.dto.subscription;

import com.conectaai.dto.customer.CustomerSummaryDto;
import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionSummaryDto(
        Long id,

        @JsonProperty("external_id")
        String externalId,

        Provider provider,

        CustomerSummaryDto customer,

        BigDecimal amount,

        Currency currency,

        SubscriptionInterval interval,

        SubscriptionStatus status,

        @JsonProperty("payment_method")
        String paymentMethod,

        @JsonProperty("start_at")
        LocalDate startAt,

        @JsonProperty("end_at")
        LocalDate endAt,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

