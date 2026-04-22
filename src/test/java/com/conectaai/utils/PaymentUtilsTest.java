package com.conectaai.utils;

import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PaymentUtilsTest {

    @Test
    void meetsMinimumAmount_whenValueAboveMinimum_returnsTrue() {
        assertTrue(PaymentUtils.meetsMinimumAmount(new BigDecimal("5.00")));
        assertTrue(PaymentUtils.meetsMinimumAmount(new BigDecimal("100.00")));
    }

    @Test
    void meetsMinimumAmount_whenValueBelowMinimum_returnsFalse() {
        assertFalse(PaymentUtils.meetsMinimumAmount(new BigDecimal("4.99")));
        assertFalse(PaymentUtils.meetsMinimumAmount(BigDecimal.ZERO));
    }

    @Test
    void meetsMinimumAmount_whenNull_returnsFalse() {
        assertFalse(PaymentUtils.meetsMinimumAmount(null));
    }

    @Test
    void isValidDueDate_whenFutureDate_returnsTrue() {
        assertTrue(PaymentUtils.isValidDueDate(LocalDate.now().plusDays(1)));
        assertTrue(PaymentUtils.isValidDueDate(LocalDate.now().plusMonths(3)));
    }

    @Test
    void isValidDueDate_whenPastDate_returnsFalse() {
        assertFalse(PaymentUtils.isValidDueDate(LocalDate.now().minusDays(1)));
    }

    @Test
    void isValidDueDate_whenNull_returnsFalse() {
        assertFalse(PaymentUtils.isValidDueDate(null));
    }

    @Test
    void isPaymentMethodSupported_whenValidMethodForProvider_returnsTrue() {
        assertTrue(PaymentUtils.isPaymentMethodSupported(Provider.ASAAS, "PIX"));
        assertTrue(PaymentUtils.isPaymentMethodSupported(Provider.ASAAS, "BOLETO"));
        assertTrue(PaymentUtils.isPaymentMethodSupported(Provider.STRIPE, "CREDIT_CARD"));
        assertTrue(PaymentUtils.isPaymentMethodSupported(Provider.MERCADO_PAGO, "PIX"));
    }

    @Test
    void isPaymentMethodSupported_whenInvalidMethodForProvider_returnsFalse() {
        assertFalse(PaymentUtils.isPaymentMethodSupported(Provider.ASAAS, "INVALID_METHOD"));
        assertFalse(PaymentUtils.isPaymentMethodSupported(Provider.STRIPE, "BOLETO"));
    }

    @Test
    void isPaymentMethodSupported_whenNullProvider_returnsFalse() {
        assertFalse(PaymentUtils.isPaymentMethodSupported(null, "PIX"));
    }

    @Test
    void isPaymentMethodSupported_whenNullMethod_returnsFalse() {
        assertFalse(PaymentUtils.isPaymentMethodSupported(Provider.ASAAS, null));
    }

    @Test
    void isCurrencySupported_whenValidCurrencyForProvider_returnsTrue() {
        assertTrue(PaymentUtils.isCurrencySupported(Provider.ASAAS, Currency.BRL));
        assertTrue(PaymentUtils.isCurrencySupported(Provider.STRIPE, Currency.USD));
        assertTrue(PaymentUtils.isCurrencySupported(Provider.STRIPE, Currency.EUR));
    }

    @Test
    void isCurrencySupported_whenInvalidCurrencyForProvider_returnsFalse() {
        assertFalse(PaymentUtils.isCurrencySupported(Provider.ASAAS, Currency.USD));
        assertFalse(PaymentUtils.isCurrencySupported(Provider.ASAAS, Currency.EUR));
    }

    @Test
    void isValidStatusTransition_whenAllowedTransition_returnsTrue() {
        assertTrue(PaymentUtils.isValidStatusTransition(PaymentStatus.PENDING, PaymentStatus.CONFIRMED));
        assertTrue(PaymentUtils.isValidStatusTransition(PaymentStatus.PENDING, PaymentStatus.CANCELLED));
        assertTrue(PaymentUtils.isValidStatusTransition(PaymentStatus.AUTHORIZED, PaymentStatus.RECEIVED));
    }

    @Test
    void isValidStatusTransition_whenSameStatus_returnsTrue() {
        assertTrue(PaymentUtils.isValidStatusTransition(PaymentStatus.PENDING, PaymentStatus.PENDING));
    }

    @Test
    void isValidStatusTransition_whenInvalidTransition_returnsFalse() {
        assertFalse(PaymentUtils.isValidStatusTransition(PaymentStatus.RECEIVED, PaymentStatus.PENDING));
        assertFalse(PaymentUtils.isValidStatusTransition(PaymentStatus.CANCELLED, PaymentStatus.CONFIRMED));
        assertFalse(PaymentUtils.isValidStatusTransition(PaymentStatus.FAILED, PaymentStatus.RECEIVED));
    }

    @Test
    void isCancellable_whenCancellableStatus_returnsTrue() {
        assertTrue(PaymentUtils.isCancellable(PaymentStatus.PENDING));
        assertTrue(PaymentUtils.isCancellable(PaymentStatus.AUTHORIZED));
        assertTrue(PaymentUtils.isCancellable(PaymentStatus.REQUIRES_ACTION));
    }

    @Test
    void isCancellable_whenNonCancellableStatus_returnsFalse() {
        assertFalse(PaymentUtils.isCancellable(PaymentStatus.RECEIVED));
        assertFalse(PaymentUtils.isCancellable(PaymentStatus.CONFIRMED));
        assertFalse(PaymentUtils.isCancellable(PaymentStatus.REFUNDED));
    }

    @Test
    void isFinalStatus_whenFinalStatus_returnsTrue() {
        assertTrue(PaymentUtils.isFinalStatus(PaymentStatus.RECEIVED));
        assertTrue(PaymentUtils.isFinalStatus(PaymentStatus.FAILED));
        assertTrue(PaymentUtils.isFinalStatus(PaymentStatus.CANCELLED));
        assertTrue(PaymentUtils.isFinalStatus(PaymentStatus.REFUNDED));
    }

    @Test
    void isFinalStatus_whenNonFinalStatus_returnsFalse() {
        assertFalse(PaymentUtils.isFinalStatus(PaymentStatus.PENDING));
        assertFalse(PaymentUtils.isFinalStatus(PaymentStatus.PROCESSING));
        assertFalse(PaymentUtils.isFinalStatus(PaymentStatus.AUTHORIZED));
    }
}