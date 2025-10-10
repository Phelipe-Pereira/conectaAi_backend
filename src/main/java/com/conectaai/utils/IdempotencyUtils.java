package com.conectaai.utils;

import org.apache.commons.lang3.StringUtils;

public final class IdempotencyUtils {
    private IdempotencyUtils() {}

    public static boolean isValidKey(String key) {
        if (StringUtils.isBlank(key)) return false;
        return key.trim().length() <= 255;
    }

    public static boolean isValidRequestHash(String hash) {
        if (StringUtils.isBlank(hash)) return false;
        return hash.trim().length() <= 255;
    }
}
