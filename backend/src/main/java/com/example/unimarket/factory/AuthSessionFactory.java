package com.example.unimarket.factory;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.util.Helper;
import com.example.unimarket.util.TokenHasher;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Creates {@link AuthSession} aggregates for refresh-token rotation.
 */
public final class AuthSessionFactory {

    private AuthSessionFactory() {
        // Factory class.
    }

    /**
     * A freshly issued session: the entity to persist and the raw refresh
     * token to place in the client cookie.
     */
    public record IssuedSession(AuthSession session, String rawRefreshToken) {
    }

    /**
     * Starts a new token family at login.
     *
     * @return the issued session, or {@code null} when the input is not valid
     */
    public static IssuedSession createSession(UUID userId,
                                              Duration timeToLive,
                                              String ipAddress,
                                              String userAgent) {

        if (userId == null || timeToLive == null || timeToLive.isNegative()) {
            return null;
        }
        return buildSession(userId, UUID.randomUUID(), timeToLive, ipAddress, userAgent);
    }

    /**
     * Creates the replacement session during rotation, keeping the existing
     * token family so that later reuse of an old token is detectable.
     *
     * @return the replacement session, or {@code null} when the input is not valid
     */
    public static IssuedSession rotateSession(AuthSession current,
                                              Duration timeToLive,
                                              String ipAddress,
                                              String userAgent) {

        if (current == null || timeToLive == null || timeToLive.isNegative()) {
            return null;
        }
        return buildSession(current.getUserId(), current.getTokenFamilyId(), timeToLive, ipAddress, userAgent);
    }

    /**
     * Returns a revoked copy of the session.
     *
     * @return the revoked session, or {@code null} when already revoked
     */
    public static AuthSession revoke(AuthSession session, RevocationReason reason) {
        if (session == null || !session.revoke(reason, Instant.now())) {
            return null;
        }
        return session;
    }

    /**
     * Returns a revoked copy that also records which session superseded it.
     */
    public static AuthSession markRotated(AuthSession session, UUID replacementSessionId) {
        if (session == null || !session.markRotated(replacementSessionId, Instant.now())) {
            return null;
        }
        return session;
    }

    private static IssuedSession buildSession(UUID userId,
                                              UUID tokenFamilyId,
                                              Duration timeToLive,
                                              String ipAddress,
                                              String userAgent) {

        String rawRefreshToken = TokenHasher.generateToken();
        Instant now = Instant.now();

        AuthSession session = new AuthSession.Builder()
                .setId(Helper.generateId())
                .setUserId(userId)
                .setTokenFamilyId(tokenFamilyId)
                .setRefreshTokenHash(TokenHasher.hash(rawRefreshToken))
                .setIssuedAt(now)
                .setExpiresAt(now.plus(timeToLive))
                .setIpHash(TokenHasher.hashSignal(ipAddress))
                .setUserAgent(truncate(userAgent))
                .build();

        return new IssuedSession(session, rawRefreshToken);
    }

    private static String truncate(String userAgent) {
        if (Helper.isNullOrEmpty(userAgent)) {
            return null;
        }
        String trimmed = userAgent.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
