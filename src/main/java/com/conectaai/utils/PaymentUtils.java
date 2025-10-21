package com.conectaai.utils;

import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public final class PaymentUtils {
    private PaymentUtils() { }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.scale() <= 2 && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidCurrency(Currency currency) {
        return currency != null;
    }

    public static boolean isValidStatus(PaymentStatus status) {
        return status != null;
    }

    public static boolean isValidPublicId(String publicId) {
        if (StringUtils.isBlank(publicId)) {
            return false;
        }
        return publicId.trim().length() <= 100;
    }

    public static boolean isValidProviderReference(String reference) {
        if (StringUtils.isBlank(reference)) {
            return true;
        }
        String ref = reference.trim();
        return ref.length() > 0 && ref.length() <= 100;
    }
}
