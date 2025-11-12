package com.conectaai.utils;

import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.RefundStatus;
import com.conectaai.enums.SubscriptionStatus;

import java.util.Locale;

public final class GatewayStatusMapper {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private GatewayStatusMapper() {
    }

    public static PaymentStatus mapPaymentStatus(String gatewayStatus) {
        if (gatewayStatus == null) {
            return PaymentStatus.PENDING;
        }
        String status = gatewayStatus.toUpperCase(Locale.ROOT);
        return switch (status) {
            case STATUS_PENDING, "AWAITING" -> PaymentStatus.PENDING;
            case "CONFIRMED", "PAID", "RECEIVED", STATUS_SUCCEEDED, "APPROVED" -> PaymentStatus.CONFIRMED;
            case "AUTHORIZED" -> PaymentStatus.AUTHORIZED;
            case "PROCESSING" -> PaymentStatus.PROCESSING;
            case "FAILED", "REJECTED" -> PaymentStatus.FAILED;
            case STATUS_CANCELLED, "CANCELED", "DELETED" -> PaymentStatus.CANCELLED;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            case "EXPIRED", "OVERDUE" -> PaymentStatus.EXPIRED;
            default -> PaymentStatus.PENDING;
        };
    }

    public static SubscriptionStatus mapSubscriptionStatus(String gatewayStatus) {
        if (gatewayStatus == null) {
            return SubscriptionStatus.PENDING;
        }
        String status = gatewayStatus.toUpperCase(Locale.ROOT);
        return switch (status) {
            case STATUS_PENDING, "CREATED" -> SubscriptionStatus.PENDING;
            case "ACTIVE", "ACTIVATED", STATUS_SUCCEEDED -> SubscriptionStatus.ACTIVE;
            case "TRIALING" -> SubscriptionStatus.TRIALING;
            case "PAST_DUE", "INACTIVATED" -> SubscriptionStatus.PAST_DUE;
            case "PAUSED" -> SubscriptionStatus.PAUSED;
            case STATUS_CANCELLED, "CANCELED", "DELETED" -> SubscriptionStatus.CANCELLED;
            case "EXPIRED" -> SubscriptionStatus.EXPIRED;
            case "INCOMPLETE" -> SubscriptionStatus.INCOMPLETE;
            case "INCOMPLETE_EXPIRED" -> SubscriptionStatus.INCOMPLETE_EXPIRED;
            case "UNPAID" -> SubscriptionStatus.UNPAID;
            default -> SubscriptionStatus.PENDING;
        };
    }

    public static RefundStatus mapRefundStatus(String gatewayStatus) {
        if (gatewayStatus == null) {
            return RefundStatus.PENDING;
        }
        String status = gatewayStatus.toUpperCase(Locale.ROOT);
        return switch (status) {
            case STATUS_PENDING -> RefundStatus.PENDING;
            case "PROCESSING" -> RefundStatus.PROCESSING;
            case "COMPLETED", STATUS_SUCCEEDED, "SUCCESS" -> RefundStatus.COMPLETED;
            case "FAILED", "REJECTED" -> RefundStatus.FAILED;
            case STATUS_CANCELLED, "CANCELED" -> RefundStatus.CANCELLED;
            default -> RefundStatus.PENDING;
        };
    }
}

