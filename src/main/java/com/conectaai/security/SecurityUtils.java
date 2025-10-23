package com.conectaai.security;

import com.conectaai.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        return null;
    }

    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        return user.getId();
    }
}

