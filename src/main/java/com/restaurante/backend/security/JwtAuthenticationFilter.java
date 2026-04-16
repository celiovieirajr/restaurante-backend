package com.restaurante.backend.security;

import com.restaurante.backend.services.TokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenService tokenService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            // 1. Parse and validate JWT signature + expiration
            String username = jwtService.extractUsername(jwt);

            // 2. Check it's not a refresh token
            if (jwtService.isRefreshToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Check token exists in DB and is not revoked
            if (!tokenService.isTokenValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Set authentication context using Database Authorities
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // We load from database to ensure granular permissions are respected in real-time
                org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException e) {
            // Invalid token — just continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}
