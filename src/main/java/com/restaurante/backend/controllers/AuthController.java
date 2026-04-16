package com.restaurante.backend.controllers;

import com.restaurante.backend.dto.IntrospectResponseDTO;
import com.restaurante.backend.dto.LoginRequestDTO;
import com.restaurante.backend.dto.LoginResponseDTO;
import com.restaurante.backend.dto.RefreshRequestDTO;
import com.restaurante.backend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            authService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso."));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Token não encontrado."));
    }

    @GetMapping("/introspect")
    public ResponseEntity<IntrospectResponseDTO> introspect(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(authService.introspect(token));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
