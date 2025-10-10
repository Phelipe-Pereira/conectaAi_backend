package com.conectaai.utils;

import com.conectaai.enums.Currency;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class SubscriptionUtils {
    private SubscriptionUtils() {}

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.scale() <= 2 && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidCurrency(Currency currency) {
        return currency != null;
    }

    public static boolean isValidInterval(SubscriptionInterval interval) {
        return interval != null;
    }

    public static boolean isValidStatus(SubscriptionStatus status) {
        return status != null;
    }

    public static boolean isValidDates(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null) return false;
        if (endAt == null) return true;
        return !endAt.isBefore(startAt);
    }
}
