package com.example.unimarket.response;

import com.example.unimarket.domain.AuthSession;

import java.time.Instant;
import java.util.UUID;

/**
 * Non-sensitive view of an active session, so a member can review where they
 * are signed in.
 *
 * <p>The stored refresh-token hash is never included.
 */
public record SessionResponse(
        UUID sessionId,
        Instant issuedAt,
        Instant expiresAt,
        Instant lastUsedAt,
        String userAgent
) {

    public static SessionResponse from(AuthSession session) {
        if (session == null) {
            return null;
        }
        return new SessionResponse(
                session.getId(),
                session.getIssuedAt(),
                session.getExpiresAt(),
                session.getLastUsedAt(),
                session.getUserAgent()
        );
    }
}
