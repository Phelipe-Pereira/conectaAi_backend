package com.conectaai.security;

import com.conectaai.domain.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User u) {
            return u.getId();
        }

        if (principal instanceof UserDetails ud) {
            return Long.valueOf(ud.getUsername());
        }

        throw new AccessDeniedException("Não foi possível determinar o usuário autenticado");
    }
}