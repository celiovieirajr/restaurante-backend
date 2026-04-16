package com.restaurante.backend.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktZm9yLWRldmVsb3BtZW50LW9ubHktZG8tbm90LXVzZS1pbi1wcm9kdWN0aW9u";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret);
        ReflectionTestUtils.setField(jwtService, "usuarioExpiration", 1800L);
        ReflectionTestUtils.setField(jwtService, "administradorExpiration", 3600L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800L);
        ReflectionTestUtils.setField(jwtService, "masterExpiration", 3153600000L);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        String username = "testuser";
        String role = "USUARIO";

        String token = jwtService.generateAccessToken(username, role);
        assertNotNull(token);

        String extractedUsername = jwtService.extractUsername(token);
        String extractedRole = jwtService.extractRole(token);

        assertEquals(username, extractedUsername);
        assertEquals(role, extractedRole);
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String username = "testuser";
        String role = "USUARIO";

        String token = jwtService.generateRefreshToken(username, role);
        assertNotNull(token);
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void shouldExtractJti() {
        String token = jwtService.generateAccessToken("user", "USUARIO");
        String jti = jwtService.extractJti(token);
        assertNotNull(jti);
        assertFalse(jti.isEmpty());
    }
}
