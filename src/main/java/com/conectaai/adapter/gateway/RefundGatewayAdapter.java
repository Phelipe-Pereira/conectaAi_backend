package com.conectaai.adapter.gateway;

import com.conectaai.domain.Payment;

import java.math.BigDecimal;

public interface RefundGatewayAdapter {

    GatewayRefundResponse createRefund(
            Payment payment,
            BigDecimal amount,
            String reason,
            String externalId
    );

    GatewayRefundResponse getRefund(String providerRefundId);
}

