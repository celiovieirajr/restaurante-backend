package com.restaurante.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    
    @Value("${jwt.expiration.usuario:1800}")
    private long usuarioExpiration;
    
    @Value("${jwt.expiration.administrador:3600}")
    private long administradorExpiration;

    @Value("${jwt.expiration.refresh:604800}")
    private long refreshExpiration;

    @Value("${jwt.expiration.master:3153600000}")
    private long masterExpiration;

    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate Access Token with role-based expiration.
     */
    public String generateAccessToken(String username, String role) {
        long expirationSec = switch (role) {
            case "MASTER" -> masterExpiration;
            case "ADMINISTRADOR" -> administradorExpiration;
            default -> usuarioExpiration;
        };

        return buildToken(username, role, expirationSec, false);
    }

    /**
     * Generate Refresh Token.
     */
    public String generateRefreshToken(String username, String role) {
        return buildToken(username, role, refreshExpiration, true);
    }

    private String buildToken(String username, String role, long expirationSec, boolean isRefresh) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("role", role)
                .claim("refresh", isRefresh)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSec)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw e;
        }
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public String extractJti(String token) {
        return parseToken(token).getId();
    }

    public boolean isRefreshToken(String token) {
        Boolean isRefresh = parseToken(token).get("refresh", Boolean.class);
        return isRefresh != null && isRefresh;
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
