package com.conectaai.controller.auth;

import com.conectaai.dto.auth.*;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final AppLogger LOGGER = AppLogger.getLogger(AuthController.class);
    private final AuthService authService;

    private Long toUserId(org.springframework.security.core.userdetails.User principal) {
        try {
            return Long.valueOf(principal.getUsername());
        } catch (NumberFormatException e) {
            LOGGER.error("toUserId", "Erro ao converter username para userId: %s", principal.getUsername());
            throw new org.springframework.security.access.AccessDeniedException("Principal inválido");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        LOGGER.info("register", "Iniciando registro para email: %s", request.email());
        AuthResponseDto response = authService.register(request);
        LOGGER.info("register", "Usuário registrado com sucesso: %s", response.user().email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LOGGER.info("login", "Tentando login para email: %s", request.email());
        AuthResponseDto response = authService.login(request);
        LOGGER.info("login", "Login realizado com sucesso para email: %s", response.user().email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User principal) {
        Long userId = toUserId(principal);
        LOGGER.info("logout", "Logout solicitado para userId: %s", userId);
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        LOGGER.info("refreshToken", "Refresh token solicitado");
        AuthResponseDto response = authService.refreshToken(request);
        LOGGER.info("refreshToken", "Refresh token gerado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        LOGGER.info("forgotPassword", "Solicitação de reset de senha para email: %s", request.email());
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        LOGGER.info("resetPassword", "Reset de senha solicitado");
        authService.resetPassword(request);
        LOGGER.info("resetPassword", "Senha resetada com sucesso");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User principal,
                                               @Valid @RequestBody ChangePasswordRequestDto request) {
        Long userId = toUserId(principal);
        LOGGER.info("changePassword", "Alteração de senha solicitada para userId: %s", userId);
        authService.changePassword(request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User principal) {
        Long userId = toUserId(principal);
        LOGGER.info("getCurrentUser", "Consultando informações para userId: %s", userId);
        UserResponseDto userResponse = authService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }
}