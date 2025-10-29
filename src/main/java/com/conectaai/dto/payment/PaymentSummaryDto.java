package com.conectaai.dto.payment;

import com.conectaai.dto.customer.CustomerSummaryDto;
import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentSummaryDto(

        Long id,

        @JsonProperty("external_id")
        String externalId,

        Provider provider,

        CustomerSummaryDto customer,

        BigDecimal amount,

        Currency currency,

        PaymentStatus status,

        @JsonProperty("payment_method")
        String paymentMethod,

        @JsonProperty("due_date")
        LocalDate dueDate,

        @JsonProperty("paid_at")
        LocalDateTime paidAt,

        @JsonProperty("payment_url")
        String paymentUrl,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

