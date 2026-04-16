package com.restaurante.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String type; // Bearer
    private String role;
    private String username;
    private Long accessTokenExpiresIn; // seconds
    private Long refreshTokenExpiresIn; // seconds
    private java.util.Set<String> permissions;
}
