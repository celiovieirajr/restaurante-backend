package com.restaurante.backend.repositories;

import com.restaurante.backend.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenHash(String tokenHash);

    Optional<Token> findByJti(String jti);

    List<Token> findByUserIdAndRevokedFalse(Long userId);

    List<Token> findByUserUsername(String username);

    List<Token> findByParentId(Long parentId);
}
