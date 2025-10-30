package com.conectaai.dto.refund;

import com.conectaai.dto.payment.PaymentSummaryDto;
import com.conectaai.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundSummaryDto(
        Long id,
        @JsonProperty("external_id")
        String externalId,
        PaymentSummaryDto payment,
        BigDecimal amount,
        RefundStatus status,
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

