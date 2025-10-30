package com.conectaai.utils;

import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class SubscriptionUtils {

    private SubscriptionUtils() {
    }

    public static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("5.00");

    private static final Map<Provider, Set<String>> SUPPORTED_PAYMENT_METHODS = Map.of(
            Provider.ASAAS, Set.of("BOLETO", "CREDIT_CARD", "DEBIT_CARD", "PIX"),
            Provider.STRIPE, Set.of("CARD", "CREDIT_CARD", "DEBIT_CARD"),
            Provider.MERCADO_PAGO, Set.of("CREDIT_CARD", "DEBIT_CARD")
    );

    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> VALID_STATUS_TRANSITIONS = Map.of(
            SubscriptionStatus.PENDING, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INCOMPLETE, SubscriptionStatus.CANCELLED),
            SubscriptionStatus.INCOMPLETE, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INCOMPLETE_EXPIRED, SubscriptionStatus.CANCELLED),
            SubscriptionStatus.TRIALING, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.UNPAID),
            SubscriptionStatus.ACTIVE, Set.of(SubscriptionStatus.PAST_DUE, SubscriptionStatus.PAUSED, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED),
            SubscriptionStatus.PAST_DUE, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, SubscriptionStatus.UNPAID),
            SubscriptionStatus.PAUSED, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED),
            SubscriptionStatus.UNPAID, Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED)
    );

    private static final Set<SubscriptionStatus> CANCELLABLE_STATUSES = Set.of(
            SubscriptionStatus.PENDING,
            SubscriptionStatus.INCOMPLETE,
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.ACTIVE,
            SubscriptionStatus.PAST_DUE,
            SubscriptionStatus.PAUSED,
            SubscriptionStatus.UNPAID
    );

    private static final Set<SubscriptionStatus> FINAL_STATUSES = Set.of(
            SubscriptionStatus.CANCELLED,
            SubscriptionStatus.EXPIRED,
            SubscriptionStatus.INCOMPLETE_EXPIRED
    );

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(MINIMUM_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                    String.format("Valor da assinatura deve ser no mínimo R$ %.2f", MINIMUM_AMOUNT)
            );
        }
    }

    public static boolean isPaymentMethodSupported(Provider provider, String paymentMethod) {
        Set<String> methods = SUPPORTED_PAYMENT_METHODS.get(provider);
        return methods != null && methods.contains(paymentMethod.toUpperCase());
    }

    public static boolean isValidStatusTransition(SubscriptionStatus currentStatus, SubscriptionStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        if (currentStatus == newStatus) {
            return false;
        }
        Set<SubscriptionStatus> allowedTransitions = VALID_STATUS_TRANSITIONS.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    public static boolean isCancellable(SubscriptionStatus status) {
        return status != null && CANCELLABLE_STATUSES.contains(status);
    }

    public static boolean isFinalStatus(SubscriptionStatus status) {
        return status != null && FINAL_STATUSES.contains(status);
    }
}
