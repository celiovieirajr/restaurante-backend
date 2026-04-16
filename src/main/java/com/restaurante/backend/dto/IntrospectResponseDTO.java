package com.restaurante.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class IntrospectResponseDTO {

    private boolean active;
    private String username;
    private String role;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private boolean nonExpiring;
    private String jti;
    private java.util.Set<String> permissions;
}
