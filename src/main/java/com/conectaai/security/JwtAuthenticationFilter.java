package com.conectaai.security;

import com.conectaai.logger.AppLogger;
import com.conectaai.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final AppLogger LOGGER = AppLogger.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        LOGGER.info("doFilterInternal", "Requisição recebida: {} {}", method, requestUri);
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("doFilterInternal", "Token ausente ou inválido para: {} {}", method, requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        LOGGER.info("doFilterInternal", "Token recebido (primeiros 20 chars): {}", jwt.substring(0, Math.min(20, jwt.length())));
        
        if (!jwtService.isTokenValid(jwt)) {
            LOGGER.error("doFilterInternal", "Token JWT inválido para: {} {}", method, requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            LOGGER.info("doFilterInternal", "Usuário já autenticado para: {} {}", method, requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String userId = jwtService.getUserIdFromToken(jwt);
        String email = jwtService.getEmailFromToken(jwt);
        List<String> roles = jwtService.getRolesFromToken(jwt);

        LOGGER.info("doFilterInternal", "Dados extraídos do token - UserId: {}, Email: {}, Roles: {}", userId, email, roles);

        Collection<? extends GrantedAuthority> authorities =
                roles == null ? List.of()
                        : roles.stream()
                        .filter(Objects::nonNull)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        LOGGER.info("doFilterInternal", "Authorities processadas: {}", authorities);

        var principal = org.springframework.security.core.userdetails.User
                .withUsername(userId)
                .password("")
                .authorities(authorities)
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false)
                .build();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        LOGGER.info("doFilterInternal", "Autenticação configurada com sucesso para userId: {} com roles: {}", userId, authorities);
        
        filterChain.doFilter(request, response);
    }
}