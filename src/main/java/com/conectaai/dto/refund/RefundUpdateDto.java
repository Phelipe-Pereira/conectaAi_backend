package com.conectaai.dto.refund;

import com.conectaai.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record RefundUpdateDto(
        RefundStatus status,
        @JsonProperty("provider_refund_id")
        String providerRefundId,
        @JsonProperty("processed_at")
        LocalDateTime processedAt
) {
}

