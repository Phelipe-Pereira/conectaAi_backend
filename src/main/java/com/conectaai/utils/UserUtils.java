package com.conectaai.utils;

import java.util.regex.Pattern;

public final class UserUtils {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final String USERNAME_REGEX = "^\\w{3,20}$";
    private static final String PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    
    private UserUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        return username.trim().toLowerCase();
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.replaceAll("[^\\d+]", "");
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        return normalized;
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 12) {
            return false;
        }
        
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    public static String generateUsername(String email) {
        if (email == null || !isValidEmail(email)) {
            return null;
        }
        
        String localPart = email.split("@")[0];
        String normalized = localPart.replaceAll("[^a-zA-Z0-9]", "");
        
        if (normalized.length() < 3) {
            normalized = "user" + System.currentTimeMillis() % 10000;
        }
        
        return normalized.toLowerCase();
    }

    public static boolean isEmailDomainValid(String email) {
        if (!isValidEmail(email)) {
            return false;
        }
        
        String domain = email.split("@")[1].toLowerCase();
        return !domain.equals("tempmail.com") && 
               !domain.equals("10minutemail.com") && 
               !domain.equals("guerrillamail.com");
    }

    public static String maskEmail(String email) {
        if (email == null || !isValidEmail(email)) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return localPart + "@" + domain;
        }
        
        String masked = localPart.charAt(0) + "*".repeat(localPart.length() - 2)
                + localPart.charAt(localPart.length() - 1);
        return masked + "@" + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        String normalized = normalizePhone(phone);
        if (normalized.length() < 4) {
            return phone;
        }
        
        String visible = normalized.substring(0, 2);
        String masked = "*".repeat(normalized.length() - 4);
        String end = normalized.substring(normalized.length() - 2);
        
        return visible + masked + end;
    }
}
