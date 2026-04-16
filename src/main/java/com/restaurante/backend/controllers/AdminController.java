package com.restaurante.backend.controllers;

import com.restaurante.backend.security.SecurityAuditLogger;
import com.restaurante.backend.services.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('USUARIO', 'ADMINISTRADOR', 'MASTER')")
public class AdminController {

    private final TokenService tokenService;

    public AdminController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(Map.of(
                "status", "Painel Administrativo Ativo",
                "message", "Acesso concedido apenas para Administradores e Master."
        ));
    }

    @GetMapping("/system/info")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Map<String, String>> getSystemInfo() {
        return ResponseEntity.ok(Map.of(
                "version", "1.0.0",
                "environment", "Production-Like (JWT Secure)",
                "access", "TOTAL (MASTER ONLY)"
        ));
    }
}
