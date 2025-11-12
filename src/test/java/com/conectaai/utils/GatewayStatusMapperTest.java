package com.conectaai.utils;

import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.RefundStatus;
import com.conectaai.enums.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayStatusMapperTest {

    @Test
    void mapPaymentStatusConfirmed() {
        PaymentStatus status = GatewayStatusMapper.mapPaymentStatus("confirmed");
        assertEquals(PaymentStatus.CONFIRMED, status);
    }

    @Test
    void mapPaymentStatusPending() {
        PaymentStatus status = GatewayStatusMapper.mapPaymentStatus("pending");
        assertEquals(PaymentStatus.PENDING, status);
    }

    @Test
    void mapPaymentStatusNull() {
        PaymentStatus status = GatewayStatusMapper.mapPaymentStatus(null);
        assertEquals(PaymentStatus.PENDING, status);
    }

    @Test
    void mapSubscriptionStatusActive() {
        SubscriptionStatus status = GatewayStatusMapper.mapSubscriptionStatus("active");
        assertEquals(SubscriptionStatus.ACTIVE, status);
    }

    @Test
    void mapSubscriptionStatusCancelled() {
        SubscriptionStatus status = GatewayStatusMapper.mapSubscriptionStatus("cancelled");
        assertEquals(SubscriptionStatus.CANCELLED, status);
    }

    @Test
    void mapRefundStatusCompleted() {
        RefundStatus status = GatewayStatusMapper.mapRefundStatus("completed");
        assertEquals(RefundStatus.COMPLETED, status);
    }

    @Test
    void mapRefundStatusPending() {
        RefundStatus status = GatewayStatusMapper.mapRefundStatus("pending");
        assertEquals(RefundStatus.PENDING, status);
    }
}

