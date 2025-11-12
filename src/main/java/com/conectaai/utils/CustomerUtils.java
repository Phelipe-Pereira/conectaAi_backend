package com.conectaai.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Set;

public final class CustomerUtils {
    private CustomerUtils() { }

    private static final Set<String> UF = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
            "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
    );

    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return email.length() <= 254 && email.contains("@");
    }

    public static boolean isValidCPF(String digits) {
        if (StringUtils.isBlank(digits)) {
            return false;
        }
        if (!digits.matches("\\d{11}")) {
            return false;
        }
        if (digits.chars().distinct().count() == 1) {
            return false;
        }
        int s1 = 0;
        int s2 = 0;
        int w1 = 10;
        int w2 = 11;
        for (int i = 0; i < 9; i++) {
            int n = digits.charAt(i) - '0';
            s1 += n * w1--;
            s2 += n * w2--;
        }
        int d1 = s1 % 11;
        d1 = d1 < 2 ? 0 : 11 - d1;
        s2 += d1 * w2;
        int d2 = s2 % 11;
        d2 = d2 < 2 ? 0 : 11 - d2;
        return digits.charAt(9) - '0' == d1 && digits.charAt(10) - '0' == d2;
    }

    public static boolean isValidCNPJ(String digits) {
        if (StringUtils.isBlank(digits)) {
            return false;
        }
        if (!digits.matches("\\d{14}")) {
            return false;
        }
        if (digits.chars().distinct().count() == 1) {
            return false;
        }
        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int s1 = 0;
        int s2 = 0;
        for (int i = 0; i < 12; i++) {
            s1 += (digits.charAt(i) - '0') * w1[i];
        }
        int d1 = s1 % 11;
        d1 = d1 < 2 ? 0 : 11 - d1;
        for (int i = 0; i < 12; i++) {
            s2 += (digits.charAt(i) - '0') * w2[i];
        }
        s2 += d1 * w2[12];
        int d2 = s2 % 11;
        d2 = d2 < 2 ? 0 : 11 - d2;
        return digits.charAt(12) - '0' == d1 && digits.charAt(13) - '0' == d2;
    }

    public static boolean isValidName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        return name.trim().length() >= 3 && name.trim().length() <= 100;
    }

    public static boolean isValidZipCode(String zip) {
        if (StringUtils.isBlank(zip)) {
            return false;
        }
        return zip.matches("\\d{8}");
    }

    public static boolean isValidState(String state) {
        if (StringUtils.isBlank(state)) {
            return false;
        }
        return UF.contains(state.toUpperCase());
    }

    public static boolean isValidBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        return birthDate.isBefore(LocalDate.now());
    }
}
