package com.example.unimarket.repository;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.enums.RevocationReason;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA access to the {@code auth_session} table. */
public interface AuthSessionJpaRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AuthSession s where s.refreshTokenHash = :refreshTokenHash")
    Optional<AuthSession> findByRefreshTokenHashForUpdate(
            @Param("refreshTokenHash") String refreshTokenHash);

    List<AuthSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(
            UUID userId, Instant now);

    boolean existsByUserIdAndTokenFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(
            UUID userId, UUID tokenFamilyId, Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AuthSession s
               set s.revokedAt = :now,
                   s.revocationReason = :reason,
                   s.version = s.version + 1
             where s.tokenFamilyId = :familyId
               and s.revokedAt is null
            """)
    int revokeFamily(@Param("familyId") UUID familyId,
                     @Param("reason") RevocationReason reason,
                     @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AuthSession s
               set s.revokedAt = :now,
                   s.revocationReason = :reason,
                   s.version = s.version + 1
             where s.userId = :userId
               and s.revokedAt is null
            """)
    int revokeAllForUser(@Param("userId") UUID userId,
                         @Param("reason") RevocationReason reason,
                         @Param("now") Instant now);
}
