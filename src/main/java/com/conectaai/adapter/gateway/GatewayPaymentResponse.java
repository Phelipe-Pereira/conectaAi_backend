package com.conectaai.adapter.gateway;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record GatewayPaymentResponse(
        String providerPaymentId,
        String status,
        BigDecimal amount,
        String currency,
        String paymentUrl,
        String qrCode,
        String barCode,
        OffsetDateTime paidAt,
        Map<String, Object> metadata
) {
}

