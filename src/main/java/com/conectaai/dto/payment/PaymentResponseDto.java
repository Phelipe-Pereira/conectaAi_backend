package com.conectaai.dto.payment;

import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        String id,
        
        @JsonProperty("public_id")
        String publicId,
        
        @JsonProperty("customer_id")
        String customerId,
        
        BigDecimal amount,
        
        Currency currency,
        
        PaymentStatus status,
        
        String description,
        
        @JsonProperty("payment_method")
        String paymentMethod,
        
        @JsonProperty("due_date")
        LocalDateTime dueDate,
        
        Provider provider,
        
        @JsonProperty("provider_reference")
        String providerReference,
        
        @JsonProperty("payment_url")
        String paymentUrl,
        
        String barcode,
        
        @JsonProperty("pix_qrcode")
        String pixQrcode,
        
        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}

