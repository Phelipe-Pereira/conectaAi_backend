package com.conectaai.utils;

import org.apache.commons.lang3.StringUtils;

public final class DataNormalizer {
    private DataNormalizer() { }

    public static String email(String email) {
        return StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

    public static String documentNumeric(String document) {
        return StringUtils.isBlank(document) ? null : StringUtils.trimToNull(document.replaceAll("\\D", ""));
    }

    public static String phone(String phone) {
        return StringUtils.isBlank(phone) ? null : StringUtils.trimToNull(phone.replaceAll("\\D", ""));
    }

    public static String state(String state) {
        return StringUtils.trimToNull(StringUtils.upperCase(state));
    }

    public static String zip(String zip) {
        return StringUtils.isBlank(zip) ? null : StringUtils.trimToNull(zip.replaceAll("\\D", ""));
    }

    public static String name(String name) {
        return StringUtils.isBlank(name) ? null : StringUtils.trimToNull(name.replaceAll("\\s+", " "));
    }

    public static String providerRef(String reference) {
        return StringUtils.trimToNull(reference);
    }

    public static String eventType(String eventType) {
        return StringUtils.trimToNull(StringUtils.lowerCase(eventType));
    }

    public static String country(String country) {
        return StringUtils.isBlank(country) ? null : StringUtils.trimToNull(country.toUpperCase());
    }

    public static String publicId(String publicId) {
        return StringUtils.trimToNull(publicId);
    }
}
