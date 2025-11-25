package com.conectaai.security;

import com.conectaai.domain.User;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.apikey.ApiKeyService;
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
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final AppLogger LOGGER = AppLogger.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String HEADER_ACCESS_TOKEN = "access_token";
    private static final String METHOD_DO_FILTER_INTERNAL = "doFilterInternal";

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(HEADER_ACCESS_TOKEN);
        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.info(METHOD_DO_FILTER_INTERNAL, "API Key encontrada no header. Tentando autenticar...");

        try {
            User user = apiKeyService.validateApiKey(apiKey);
            if (user == null || !Boolean.TRUE.equals(user.getActive())) {
                if (user != null) {
                    LOGGER.warn(METHOD_DO_FILTER_INTERNAL, "Usuário inativo. UserId: {}", user.getId());
                } else {
                    LOGGER.warn(METHOD_DO_FILTER_INTERNAL, "API Key inválida ou expirada");
                }
                filterChain.doFilter(request, response);
                return;
            }

            Collection<? extends GrantedAuthority> authorities = buildAuthorities(user);
            setAuthentication(user, authorities, request);

            LOGGER.info(METHOD_DO_FILTER_INTERNAL,
                    "Autenticação via API Key configurada com sucesso. UserId: {}, Roles: {}",
                    user.getId(), authorities);

        } catch (Exception e) {
            LOGGER.error(METHOD_DO_FILTER_INTERNAL, "Erro ao validar API Key: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        return user.getRoleNames().stream()
                .filter(Objects::nonNull)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private void setAuthentication(User user, Collection<? extends GrantedAuthority> authorities,
                                    HttpServletRequest request) {
        var principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password("")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}

