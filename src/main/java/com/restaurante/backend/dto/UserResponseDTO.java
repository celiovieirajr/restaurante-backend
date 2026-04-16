package com.restaurante.backend.dto;

import com.restaurante.backend.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;
    private java.util.Set<String> permissions;
    private LocalDateTime createdAt;
}
