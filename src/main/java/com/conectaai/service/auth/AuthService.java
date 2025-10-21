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
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {
    
    private static final AppLogger logger = AppLogger.getLogger(AuthService.class);
    
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
                logger.warn("validateUserDoesNotExist", "Tentativa de registro com username já em uso: {}", 
                    registerRequest.username());
            }
            if (emailExists) {
                logger.warn("validateUserDoesNotExist", "Tentativa de registro com email já em uso: {}", 
                    registerRequest.email());
            }
            throw new UserAlreadyExistsException("Username ou email já estão em uso");
        }
    }

    private User createUser(RegisterRequestDto registerRequest) {
        String hashedPassword = passwordEncoder.encode(registerRequest.password());

        return User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(hashedPassword)
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
                    logger.warn("verifyPassword", "Tentativa de login com email não cadastrado: {}", email);
                    return new InvalidCredentialsException("Credenciais inválidas");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("verifyPassword", "Tentativa de login com senha incorreta para email: {}", email);
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        logger.info("verifyPassword", "Login bem-sucedido para email: {}", email);
        return user;
    }

    private void verifyUserIsActive(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            logger.warn("verifyUserIsActive", "Tentativa de acesso com usuário inativo. UserId: {}", user.getId());
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

            logger.info("register", "Usuário registrado com sucesso: {}", registerRequest.username());
            return buildAuthResponse(user, accessToken, refreshToken.getToken());
            
        } catch (DataIntegrityViolationException e) {
            logger.warn("register", "Tentativa de registro com username/email duplicado: {} / {}", 
                registerRequest.username(), registerRequest.email());
            throw new UserAlreadyExistsException("Username ou email já estão em uso");
        }
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        User user = verifyPassword(loginRequest.email(), loginRequest.password());
        verifyUserIsActive(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Token inválido"));

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException("Token expirado ou revogado");
        }

        User user = refreshToken.getUser();
        verifyUserIsActive(user);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Token inválido"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequest) {
        var userOptional = userRepository.findByEmail(forgotPasswordRequest.email());
        
        if (userOptional.isEmpty()) {
            logger.warn("forgotPassword", "Tentativa de recuperação para email não cadastrado: {}", 
                forgotPasswordRequest.email());
            return;
        }
        
        User user = userOptional.get();
        passwordResetTokenRepository.revokeAllByUserId(user.getId());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        passwordResetTokenRepository.save(resetToken);
        logger.info("forgotPassword", "Token de reset gerado para email: {}", forgotPasswordRequest.email());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequest) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetPasswordRequest.token())
                .orElseThrow(() -> new InvalidResetTokenException("Token inválido"));

        if (!resetToken.isValid()) {
            throw new InvalidResetTokenException("Token expirado ou já utilizado");
        }

        User user = resetToken.getUser();
        
        if (passwordEncoder.matches(resetPasswordRequest.newPassword(), user.getPassword())) {
            logger.warn("resetPassword", "Tentativa de reset com senha igual à atual. UserId: {}", user.getId());
            throw new InvalidPasswordException("A nova senha não pode ser igual à senha atual");
        }
        
        String hashedPassword = passwordEncoder.encode(resetPasswordRequest.newPassword());
        user.setPassword(hashedPassword);

        resetToken.markAsUsed();

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUserId(user.getId());
        
        logger.info("resetPassword", "Senha resetada e todos os tokens revogados para userId: {}", user.getId());
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto changePasswordRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("changePassword", "Usuário autenticado não encontrado. UserId: {}", userId);
                    return new UserNotFoundException("Erro ao processar requisição");
                });

        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPassword())) {
            logger.warn("changePassword", "Senha atual incorreta para userId: {}", userId);
            throw new InvalidPasswordException("Senha atual incorreta");
        }
        
        if (passwordEncoder.matches(changePasswordRequest.newPassword(), user.getPassword())) {
            logger.warn("changePassword", "Tentativa de alteração para senha igual à atual. UserId: {}", userId);
            throw new InvalidPasswordException("A nova senha não pode ser igual à senha atual");
        }

        String hashedPassword = passwordEncoder.encode(changePasswordRequest.newPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId);
        
        logger.info("changePassword", "Senha alterada com sucesso para userId: {}", userId);
    }
}
