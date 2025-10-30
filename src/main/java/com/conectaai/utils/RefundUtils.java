package com.conectaai.utils;

import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.RefundStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class RefundUtils {

    private RefundUtils() {
    }

    public static final Set<PaymentStatus> REFUNDABLE_PAYMENT_STATUSES = Set.of(
            PaymentStatus.CONFIRMED,
            PaymentStatus.RECEIVED
    );

    public static final Map<RefundStatus, Set<RefundStatus>> VALID_STATUS_TRANSITIONS = Map.of(
            RefundStatus.PENDING, Set.of(RefundStatus.PROCESSING, RefundStatus.FAILED, RefundStatus.CANCELLED),
            RefundStatus.PROCESSING, Set.of(RefundStatus.COMPLETED, RefundStatus.FAILED),
            RefundStatus.COMPLETED, Set.of(),
            RefundStatus.FAILED, Set.of(),
            RefundStatus.CANCELLED, Set.of()
    );

    public static final Set<RefundStatus> CANCELLABLE_STATUSES = Set.of(
            RefundStatus.PENDING
    );

    public static final Set<RefundStatus> FINAL_STATUSES = Set.of(
            RefundStatus.COMPLETED,
            RefundStatus.FAILED,
            RefundStatus.CANCELLED
    );

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPaymentRefundable(PaymentStatus paymentStatus) {
        return REFUNDABLE_PAYMENT_STATUSES.contains(paymentStatus);
    }

    public static boolean isValidStatusTransition(RefundStatus currentStatus, RefundStatus newStatus) {
        return VALID_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus);
    }

    public static boolean isCancellable(RefundStatus status) {
        return CANCELLABLE_STATUSES.contains(status);
    }

    public static boolean isFinalStatus(RefundStatus status) {
        return FINAL_STATUSES.contains(status);
    }
}
