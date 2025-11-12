package com.conectaai.utils;

import com.conectaai.logger.AppLogger;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class IdempotencyUtils {
    private static final AppLogger LOGGER = AppLogger.getLogger(IdempotencyUtils.class);
    private static final int MAX_HASH_LENGTH = 255;

    private IdempotencyUtils() { }

    public static boolean isValidKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return key.trim().length() <= MAX_HASH_LENGTH;
    }

    public static boolean isValidRequestHash(String hash) {
        if (StringUtils.isBlank(hash)) {
            return false;
        }
        return hash.trim().length() <= MAX_HASH_LENGTH;
    }

    public static String calculateRequestHash(String payload) {
        if (StringUtils.isBlank(payload)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("calculateRequestHash", "SHA-256 não disponível, usando fallback hashCode", e);
            String fallbackHash = Integer.toHexString(payload.hashCode());
            if (fallbackHash.length() > MAX_HASH_LENGTH) {
                return fallbackHash.substring(0, MAX_HASH_LENGTH);
            }
            return fallbackHash;
        }
    }
}
