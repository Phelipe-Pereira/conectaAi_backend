package com.conectaai.utils;

import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public final class PaymentUtils {

    private static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("5.00");
    private static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    private static final String PAYMENT_METHOD_DEBIT_CARD = "DEBIT_CARD";

    private static final Map<Provider, Set<String>> SUPPORTED_PAYMENT_METHODS = Map.of(
            Provider.ASAAS, Set.of("PIX", "BOLETO", PAYMENT_METHOD_CREDIT_CARD, PAYMENT_METHOD_DEBIT_CARD),
            Provider.STRIPE, Set.of(PAYMENT_METHOD_CREDIT_CARD, PAYMENT_METHOD_DEBIT_CARD, "PIX"),
            Provider.MERCADO_PAGO, Set.of("PIX", "BOLETO", PAYMENT_METHOD_CREDIT_CARD, PAYMENT_METHOD_DEBIT_CARD)
    );

    private static final Map<Provider, Set<Currency>> SUPPORTED_CURRENCIES = Map.of(
            Provider.ASAAS, Set.of(Currency.BRL),
            Provider.STRIPE, Set.of(Currency.BRL, Currency.USD, Currency.EUR),
            Provider.MERCADO_PAGO, Set.of(Currency.BRL, Currency.ARS, Currency.MXN)
    );

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_STATUS_TRANSITIONS = Map.ofEntries(
            Map.entry(PaymentStatus.PENDING, Set.of(
                    PaymentStatus.PROCESSING, PaymentStatus.AWAITING_RISK_ANALYSIS,
                    PaymentStatus.AUTHORIZED, PaymentStatus.CONFIRMED, PaymentStatus.RECEIVED,
                    PaymentStatus.FAILED, PaymentStatus.EXPIRED, PaymentStatus.CANCELLED
            )),
            Map.entry(PaymentStatus.AWAITING_PAYMENT_METHOD, Set.of(
                    PaymentStatus.PROCESSING, PaymentStatus.CANCELLED
            )),
            Map.entry(PaymentStatus.AWAITING_RISK_ANALYSIS, Set.of(
                    PaymentStatus.PROCESSING, PaymentStatus.FAILED
            )),
            Map.entry(PaymentStatus.PROCESSING, Set.of(
                    PaymentStatus.RECEIVED, PaymentStatus.FAILED, PaymentStatus.REQUIRES_ACTION
            )),
            Map.entry(PaymentStatus.REQUIRES_ACTION, Set.of(
                    PaymentStatus.PROCESSING, PaymentStatus.CANCELLED
            )),
            Map.entry(PaymentStatus.AUTHORIZED, Set.of(
                    PaymentStatus.RECEIVED, PaymentStatus.CANCELLED, PaymentStatus.EXPIRED
            )),
            Map.entry(PaymentStatus.CONFIRMED, Set.of(
                    PaymentStatus.RECEIVED
            )),
            Map.entry(PaymentStatus.RECEIVED, Set.of(
                    PaymentStatus.REFUND_REQUESTED, PaymentStatus.REFUNDED,
                    PaymentStatus.PARTIALLY_REFUNDED, PaymentStatus.CHARGEBACK_REQUESTED
            ))
    );

    private static final Set<PaymentStatus> CANCELLABLE_STATUSES = Set.of(
            PaymentStatus.PENDING, PaymentStatus.AWAITING_PAYMENT_METHOD,
            PaymentStatus.AWAITING_RISK_ANALYSIS, PaymentStatus.REQUIRES_ACTION,
            PaymentStatus.AUTHORIZED
    );

    private static final Set<PaymentStatus> REFUNDABLE_STATUSES = Set.of(
            PaymentStatus.RECEIVED
    );

    private PaymentUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.scale() <= 2 && amount.compareTo(MINIMUM_AMOUNT) >= 0;
    }

    public static boolean meetsMinimumAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(MINIMUM_AMOUNT) >= 0;
    }

    public static boolean isValidDueDate(LocalDate dueDate) {
        return dueDate != null && !dueDate.isBefore(LocalDate.now());
    }

    public static boolean isPaymentMethodSupported(Provider provider, String paymentMethod) {
        if (provider == null || StringUtils.isBlank(paymentMethod)) {
            return false;
        }
        Set<String> supportedMethods = SUPPORTED_PAYMENT_METHODS.get(provider);
        return supportedMethods != null && supportedMethods.contains(paymentMethod.toUpperCase());
    }

    public static boolean isCurrencySupported(Provider provider, Currency currency) {
        if (provider == null || currency == null) {
            return false;
        }
        Set<Currency> supportedCurrencies = SUPPORTED_CURRENCIES.get(provider);
        return supportedCurrencies != null && supportedCurrencies.contains(currency);
    }

    public static boolean isValidStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        if (currentStatus == newStatus) {
            return true;
        }
        Set<PaymentStatus> allowedTransitions = VALID_STATUS_TRANSITIONS.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    public static boolean isCancellable(PaymentStatus status) {
        return status != null && CANCELLABLE_STATUSES.contains(status);
    }

    public static boolean isRefundable(PaymentStatus status) {
        return status != null && REFUNDABLE_STATUSES.contains(status);
    }

    public static boolean isFinalStatus(PaymentStatus status) {
        return status == PaymentStatus.RECEIVED
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.EXPIRED
                || status == PaymentStatus.CANCELLED
                || status == PaymentStatus.REFUNDED
                || status == PaymentStatus.CHARGEBACK_REQUESTED;
    }
}
