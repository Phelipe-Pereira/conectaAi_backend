package com.conectaai.adapter.gateway;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record GatewayRefundResponse(
        String providerRefundId,
        String status,
        BigDecimal amount,
        OffsetDateTime processedAt,
        Map<String, Object> metadata
) {
}

