package com.conectaai.dto.refund;

import com.conectaai.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponseDto(
        String id,
        
        @JsonProperty("payment_id")
        String paymentId,
        
        BigDecimal amount,
        
        RefundStatus status,
        
        String reason,
        
        @JsonProperty("provider_reference")
        String providerReference,
        
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

