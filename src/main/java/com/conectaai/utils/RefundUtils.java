package com.conectaai.utils;

import com.conectaai.enums.RefundStatus;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public final class RefundUtils {
    private RefundUtils() {}

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.scale() <= 2 && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isValidStatus(RefundStatus status) {
        return status != null;
    }

    public static boolean isValidProviderReference(String reference) {
        if (StringUtils.isBlank(reference)) return true;
        String ref = reference.trim();
        return ref.length() > 0 && ref.length() <= 100;
    }
}
