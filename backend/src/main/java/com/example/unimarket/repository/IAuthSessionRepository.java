package com.example.unimarket.repository;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.enums.RevocationReason;

import java.util.List;
import java.util.UUID;

/** Abstraction over {@link AuthSession} persistence. */
public interface IAuthSessionRepository extends IRepository<AuthSession, UUID> {
    AuthSession readByRawToken(String rawRefreshToken);
    AuthSession readByRawTokenForUpdate(String rawRefreshToken);
    List<AuthSession> readActiveByUserId(UUID userId);

    /** True only while this account still has an active session in the JWT's family. */
    boolean hasActiveSessionFamily(UUID userId, UUID tokenFamilyId);

    int revokeFamily(UUID tokenFamilyId, RevocationReason reason);
    int revokeAllForUser(UUID userId, RevocationReason reason);
}
