package com.conectaai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

public final class WebhookUtils {
    private WebhookUtils() {}

    public static boolean isValidEventType(String eventType) {
        if (StringUtils.isBlank(eventType)) return false;
        return eventType.trim().length() > 0 && eventType.trim().length() <= 100;
    }

    public static boolean isValidPayload(String payload) {
        if (StringUtils.isBlank(payload)) return false;
        try {
            new ObjectMapper().readTree(payload);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
