package com.conectaai.service.auth;

import com.conectaai.domain.PasswordResetToken;
import com.conectaai.domain.RefreshToken;
import com.conectaai.domain.User;
import com.conectaai.domain.UserRole;
import com.conectaai.dto.auth.*;
import com.conectaai.exception.*;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.PasswordResetTokenRepository;
import com.conectaai.repository.RefreshTokenRepository;
import com.conectaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final AppLogger LOGGER = AppLogger.getLogger(AuthService.class);

    private static final String CREDENTIALS_INVALID = "Credenciais inválidas";
    private static final String TOKEN_INVALID = "Token inválido";
    private static final String PASSWORD_CANNOT_BE_SAME = "A nova senha não pode ser igual à senha atual";
    private static final String METHOD_VERIFY_PASSWORD = "verifyPassword";
    private static final String METHOD_CHANGE_PASSWORD = "changePassword";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private void validateUserDoesNotExist(RegisterRequestDto registerRequest) {
        boolean usernameExists = userRepository.existsByUsername(registerRequest.username());
        boolean emailExists = userRepository.existsByEmail(registerRequest.email());
        if (usernameExists || emailExists) {
            if (usernameExists) {
                LOGGER.warn("validateUserDoesNotExist",
                        "Tentativa de registro com username já em uso: {}", registerRequest.username());
            }
            if (emailExists) {
                LOGGER.warn("validateUserDoesNotExist",
                        "Tentativa de registro com email já em uso: {}", registerRequest.email());
            }
            throw new UserAlreadyExistsException("Username ou email já estão em uso");
        }
    }

    private User createUser(RegisterRequestDto registerRequest) {
        String hashedPassword = passwordEncoder.encode(registerRequest.password());
        String normalizedCpfCnpj = com.conectaai.utils.DataNormalizer.cpfCnpj(registerRequest.cpfCnpj());
        return User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(hashedPassword)
                .cpfCnpj(normalizedCpfCnpj)
                .active(true)
                .roles(new ArrayList<>())
                .permissions(new ArrayList<>())
                .preferences(new ArrayList<>())
                .build();
    }

    private void addDefaultRole(User user) {
        UserRole userRole = UserRole.builder()
                .user(user)
                .role("USER")
                .build();
        user.getRoles().add(userRole);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponseDto userResponse = new UserResponseDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getActive(),
                user.getRoleNames(),
                user.getCreatedAt()
        );
        int expiresInSeconds = (int) (accessTokenExpiration / 1000);
        return new AuthResponseDto(
                userResponse,
                accessToken,
                refreshToken,
                expiresInSeconds
        );
    }

    private User verifyPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn(METHOD_VERIFY_PASSWORD, "Tentativa de login com email não cadastrado: {}", email);
                    return new InvalidCredentialsException(CREDENTIALS_INVALID);
                });
        if (!passwordEncoder.matches(password, user.getPassword())) {
            LOGGER.warn(METHOD_VERIFY_PASSWORD, "Tentativa de login com senha incorreta para email: {}", email);
            throw new InvalidCredentialsException(CREDENTIALS_INVALID);
        }
        LOGGER.info(METHOD_VERIFY_PASSWORD, "Login bem-sucedido para email: {}", email);
        return user;
    }

    private void verifyUserIsActive(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            LOGGER.warn("verifyUserIsActive", "Tentativa de acesso com usuário inativo. UserId: {}", user.getId());
            throw new UserInactiveException("Usuário inativo");
        }
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        validateUserDoesNotExist(registerRequest);
        try {
            User user = createUser(registerRequest);
            addDefaultRole(user);
            user = userRepository.save(user);
            String accessToken = jwtService.generateAccessToken(user);
            RefreshToken refreshToken = createRefreshToken(user);
            LOGGER.info("register", "Usuário registrado com sucesso: {}", registerRequest.username());
            return buildAuthResponse(user, accessToken, refreshToken.getToken());
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn("register",
                    "Tentativa de registro com username/email duplicado: {} / {}",
                    registerRequest.username(), registerRequest.email());
            throw new UserAlreadyExistsException("Username ou email já estão em uso");
        }
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        User user = verifyPassword(loginRequest.email(), loginRequest.password());
        verifyUserIsActive(user);
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException(TOKEN_INVALID));
        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException("Token expirado ou revogado");
        }
        User user = refreshToken.getUser();
        verifyUserIsActive(user);
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);
        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequest) {
        var userOptional = userRepository.findByEmail(forgotPasswordRequest.email());
        if (userOptional.isEmpty()) {
            LOGGER.warn("forgotPassword",
                    "Tentativa de recuperação para email não cadastrado: {}", forgotPasswordRequest.email());
            return;
        }
        User user = userOptional.get();
        passwordResetTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(resetToken);
        LOGGER.info("forgotPassword", "Token de reset gerado para email: {}", forgotPasswordRequest.email());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequest) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.token())
                .orElseThrow(() -> new InvalidResetTokenException(TOKEN_INVALID));
        if (!resetToken.isValid()) {
            throw new InvalidResetTokenException("Token expirado ou já utilizado");
        }
        User user = resetToken.getUser();
        if (passwordEncoder.matches(resetPasswordRequest.newPassword(), user.getPassword())) {
            LOGGER.warn("resetPassword", "Tentativa de reset com senha igual à atual. UserId: {}", user.getId());
            throw new InvalidPasswordException(PASSWORD_CANNOT_BE_SAME);
        }
        String hashedPassword = passwordEncoder.encode(resetPasswordRequest.newPassword());
        user.setPassword(hashedPassword);
        resetToken.markAsUsed();
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now());
        LOGGER.info("resetPassword", "Senha resetada e todos os tokens revogados para userId: {}", user.getId());
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto changePasswordRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error(METHOD_CHANGE_PASSWORD, "Usuário autenticado não encontrado. UserId: {}", userId);
                    return new UserNotFoundException("Erro ao processar requisição");
                });
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPassword())) {
            LOGGER.warn(METHOD_CHANGE_PASSWORD, "Senha atual incorreta para userId: {}", userId);
            throw new InvalidPasswordException("Senha atual incorreta");
        }
        if (passwordEncoder.matches(changePasswordRequest.newPassword(), user.getPassword())) {
            LOGGER.warn(METHOD_CHANGE_PASSWORD, "Tentativa de alteração para senha igual à atual. UserId: {}", userId);
            throw new InvalidPasswordException(PASSWORD_CANNOT_BE_SAME);
        }
        String hashedPassword = passwordEncoder.encode(changePasswordRequest.newPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
        LOGGER.info(METHOD_CHANGE_PASSWORD, "Senha alterada com sucesso para userId: {}", userId);
    }

    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        List<String> roleNames = user.getRoles().stream()
                .map(UserRole::getRole)
                .toList();
        return new UserResponseDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getActive(),
                roleNames,
                user.getCreatedAt()
        );
    }

    public User getUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException("Token não fornecido");
        }
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtService.isTokenValid(cleanToken)) {
            throw new InvalidCredentialsException("Token inválido");
        }
        String userId = jwtService.getUserIdFromToken(cleanToken);
        User user = userRepository.findByIdWithRoles(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        verifyUserIsActive(user);
        return user;
    }
}