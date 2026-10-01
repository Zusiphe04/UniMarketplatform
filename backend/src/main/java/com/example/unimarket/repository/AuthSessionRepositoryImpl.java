package com.example.unimarket.repository;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.util.Helper;
import com.example.unimarket.util.TokenHasher;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Singleton implementation of {@link IAuthSessionRepository}. */
@Repository
public class AuthSessionRepositoryImpl implements IAuthSessionRepository {
    private static volatile AuthSessionRepositoryImpl instance;
    private final AuthSessionJpaRepository jpaRepository;

    public AuthSessionRepositoryImpl(AuthSessionJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    public static AuthSessionRepositoryImpl getInstance(AuthSessionJpaRepository jpaRepository) {
        if (instance == null) {
            synchronized (AuthSessionRepositoryImpl.class) {
                if (instance == null) instance = new AuthSessionRepositoryImpl(jpaRepository);
            }
        }
        return instance;
    }

    @Override public AuthSession create(AuthSession entity) { return entity == null ? null : jpaRepository.save(entity); }
    @Override public AuthSession read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public AuthSession update(AuthSession entity) {
        if (entity == null || entity.getId() == null || !jpaRepository.existsById(entity.getId())) return null;
        return jpaRepository.save(entity);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<AuthSession> getAll() { return jpaRepository.findAll(); }

    @Override public AuthSession readByRawToken(String rawRefreshToken) {
        return Helper.isNullOrEmpty(rawRefreshToken) ? null
                : jpaRepository.findByRefreshTokenHash(TokenHasher.hash(rawRefreshToken)).orElse(null);
    }
    @Override public AuthSession readByRawTokenForUpdate(String rawRefreshToken) {
        return Helper.isNullOrEmpty(rawRefreshToken) ? null
                : jpaRepository.findByRefreshTokenHashForUpdate(TokenHasher.hash(rawRefreshToken)).orElse(null);
    }
    @Override public List<AuthSession> readActiveByUserId(UUID userId) {
        return userId == null ? List.of()
                : jpaRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(userId, Instant.now());
    }
    @Override public boolean hasActiveSessionFamily(UUID userId, UUID tokenFamilyId) {
        return userId != null && tokenFamilyId != null
                && jpaRepository.existsByUserIdAndTokenFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(
                        userId, tokenFamilyId, Instant.now());
    }
    @Override @Transactional public int revokeFamily(UUID tokenFamilyId, RevocationReason reason) {
        return tokenFamilyId == null || reason == null ? 0 : jpaRepository.revokeFamily(tokenFamilyId, reason, Instant.now());
    }
    @Override @Transactional public int revokeAllForUser(UUID userId, RevocationReason reason) {
        return userId == null || reason == null ? 0 : jpaRepository.revokeAllForUser(userId, reason, Instant.now());
    }
}
