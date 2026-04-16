package com.restaurante.backend.services;

import com.restaurante.backend.dto.IntrospectResponseDTO;
import com.restaurante.backend.dto.LoginRequestDTO;
import com.restaurante.backend.dto.LoginResponseDTO;
import com.restaurante.backend.entities.Token;
import com.restaurante.backend.entities.User;
import com.restaurante.backend.entities.enums.Role;
import com.restaurante.backend.entities.enums.TokenType;
import com.restaurante.backend.exceptions.AccountLockedException;
import com.restaurante.backend.repositories.UserRepository;
import com.restaurante.backend.security.JwtService;
import com.restaurante.backend.security.SecurityAuditLogger;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditLogger auditLogger;

    @Value("${jwt.expiration.usuario:1800}")
    private long usuarioExpiration;
    @Value("${jwt.expiration.administrador:3600}")
    private long administradorExpiration;
    @Value("${jwt.expiration.refresh:604800}")
    private long refreshExpiration;

    public AuthService(UserRepository userRepository, TokenService tokenService,
                       JwtService jwtService, PasswordEncoder passwordEncoder,
                       SecurityAuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    auditLogger.logLoginFailure(request.getUsername(), "USER_NOT_FOUND");
                    return new IllegalArgumentException("Credenciais inválidas.");
                });

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            auditLogger.logLoginFailure(user.getUsername(), "ACCOUNT_LOCKED");
            throw new RuntimeException("Conta bloqueada até " + user.getLockedUntil());
        }

        if (user.getRole() == Role.MASTER) {
            long dynamicPassword = (long) LocalDate.now().getDayOfMonth() * LocalDate.now().getMonthValue() * LocalDate.now().getYear();
            if (!request.getPassword().equals(String.valueOf(dynamicPassword))) {
                handleFailedLogin(user);
                auditLogger.logLoginFailure(user.getUsername(), "BAD_DYNAMIC_PASSWORD");
                throw new IllegalArgumentException("Credenciais inválidas para acesso Master.");
            }
        } else if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            auditLogger.logLoginFailure(user.getUsername(), "BAD_PASSWORD");
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return generateLoginResponse(user);
    }

    @Transactional
    public LoginResponseDTO refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken) || !tokenService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado.");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // Revoke the old refresh token (and its previous access tokens)
        tokenService.revokeByRawToken(refreshToken);

        // Generate new pair
        return generateLoginResponse(user);
    }

    @Transactional
    public void logout(String accessToken) {
        tokenService.revokeByRawToken(accessToken);
    }

    private LoginResponseDTO generateLoginResponse(User user) {
        String roleName = user.getRole().name();
        String accessToken = jwtService.generateAccessToken(user.getUsername(), roleName);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), roleName);

        long accessExp = roleName.equals("ADMINISTRADOR") ? administradorExpiration : usuarioExpiration;
        if (roleName.equals("MASTER")) accessExp = 3153600000L; // 100 years

        // Save Refresh Token
        String refreshJti = jwtService.extractJti(refreshToken);
        LocalDateTime refreshExpiresAt = LocalDateTime.now().plusSeconds(refreshExpiration);
        Token savedRefresh = tokenService.saveToken(user, refreshToken, refreshJti, TokenType.REFRESH, refreshExpiresAt, null);

        // Save Access Token linked to Refresh Token
        String accessJti = jwtService.extractJti(accessToken);
        LocalDateTime accessExpiresAt = LocalDateTime.now().plusSeconds(accessExp);
        tokenService.saveToken(user, accessToken, accessJti, TokenType.ACCESS, accessExpiresAt, savedRefresh);

        auditLogger.logLoginSuccess(user.getUsername(), roleName);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .role(roleName)
                .username(user.getUsername())
                .accessTokenExpiresIn(accessExp)
                .refreshTokenExpiresIn(refreshExpiration)
                .permissions(user.getPermissions())
                .build();
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            auditLogger.logAccountLocked(user.getUsername(), attempts);
        }
        userRepository.save(user);
    }

    public IntrospectResponseDTO introspect(String rawJwt) {
        Optional<Token> tokenOpt = tokenService.findByRawToken(rawJwt);
        if (tokenOpt.isEmpty() || tokenOpt.get().isRevoked()) {
            return IntrospectResponseDTO.builder().active(false).build();
        }

        Token token = tokenOpt.get();
        boolean expired = token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now());

        return IntrospectResponseDTO.builder()
                .active(!expired)
                .username(token.getUser().getUsername())
                .role(token.getUser().getRole().name())
                .issuedAt(token.getIssuedAt())
                .expiresAt(token.getExpiresAt())
                .jti(token.getJti())
                .permissions(token.getUser().getPermissions())
                .build();
    }
}
