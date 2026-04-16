package com.restaurante.backend.services;

import com.restaurante.backend.entities.Token;
import com.restaurante.backend.entities.User;
import com.restaurante.backend.entities.enums.TokenType;
import com.restaurante.backend.repositories.TokenRepository;
import com.restaurante.backend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Token saveToken(User user, String rawJwt, String jti, TokenType type, LocalDateTime expiresAt, Token parent) {
        Token token = new Token();
        token.setUser(user);
        token.setTokenHash(JwtService.hashToken(rawJwt));
        token.setJti(jti);
        token.setType(type);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(expiresAt);
        token.setParent(parent);
        token.setRevoked(false);
        return tokenRepository.save(token);
    }

    public boolean isTokenValid(String rawJwt) {
        String hash = JwtService.hashToken(rawJwt);
        Optional<Token> tokenOpt = tokenRepository.findByTokenHash(hash);
        return tokenOpt.isPresent() && !tokenOpt.get().isRevoked();
    }

    @Transactional
    public boolean revokeByRawToken(String rawJwt) {
        String hash = JwtService.hashToken(rawJwt);
        Optional<Token> tokenOpt = tokenRepository.findByTokenHash(hash);
        if (tokenOpt.isPresent() && !tokenOpt.get().isRevoked()) {
            Token token = tokenOpt.get();
            revokeTokenAndChildren(token);
            return true;
        }
        return false;
    }

    private void revokeTokenAndChildren(Token token) {
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        tokenRepository.save(token);
        
        // If this was a refresh token, revoke all access tokens derived from it
        if (token.getType() == TokenType.REFRESH) {
            List<Token> children = tokenRepository.findByParentId(token.getId());
            children.forEach(child -> {
                if (!child.isRevoked()) {
                    child.setRevoked(true);
                    child.setRevokedAt(LocalDateTime.now());
                }
            });
            tokenRepository.saveAll(children);
        }
    }

    @Transactional
    public int revokeAllUserTokens(Long userId) {
        List<Token> activeTokens = tokenRepository.findByUserIdAndRevokedFalse(userId);
        activeTokens.forEach(token -> {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
        });
        tokenRepository.saveAll(activeTokens);
        return activeTokens.size();
    }

    public Optional<Token> findByRawToken(String rawJwt) {
        String hash = JwtService.hashToken(rawJwt);
        return tokenRepository.findByTokenHash(hash);
    }
}
