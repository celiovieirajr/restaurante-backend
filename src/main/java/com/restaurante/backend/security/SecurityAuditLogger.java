package com.restaurante.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");

    public void logLoginSuccess(String username, String role) {
        log.info("LOGIN_SUCCESS | user={} | role={}", username, role);
    }

    public void logLoginFailure(String username, String reason) {
        log.warn("LOGIN_FAILURE | user={} | reason={}", username, reason);
    }

    public void logTokenIssued(String username, String jti, boolean nonExpiring) {
        log.info("TOKEN_ISSUED | user={} | jti={} | nonExpiring={}", username, jti, nonExpiring);
    }

    public void logTokenRevoked(String username, String jti) {
        log.info("TOKEN_REVOKED | user={} | jti={}", username, jti);
    }

    public void logAuthorizationFailure(String username, String method, String path) {
        log.warn("AUTHORIZATION_FAILURE | user={} | method={} | path={}", username, method, path);
    }

    public void logAccountLocked(String username, int attempts) {
        log.warn("ACCOUNT_LOCKED | user={} | failedAttempts={}", username, attempts);
    }
}
