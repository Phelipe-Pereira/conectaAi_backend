package com.conectaai.adapter.gateway;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record GatewaySubscriptionResponse(
        String providerSubscriptionId,
        String status,
        BigDecimal amount,
        String currency,
        OffsetDateTime currentPeriodStart,
        OffsetDateTime currentPeriodEnd,
        OffsetDateTime canceledAt,
        Map<String, Object> metadata
) {
}

