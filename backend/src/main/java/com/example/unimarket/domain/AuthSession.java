package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.RevocationReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-side record of one refresh token.
 *
 * <p>Each successful login starts a token family. Every rotation revokes the
 * presented session and inserts a replacement in the same family, so a token
 * that is presented after it was already exchanged identifies theft or a
 * client bug, and the whole family can be revoked at once.
 *
 * <p>Only a hash of the refresh token is stored.
 */
@Entity
@Table(
        name = "auth_session",
        uniqueConstraints = @UniqueConstraint(name = "uk_auth_session_token", columnNames = "refresh_token_hash"),
        indexes = {
                @Index(name = "idx_auth_session_user", columnList = "user_id, revoked_at, expires_at"),
                @Index(name = "idx_auth_session_family", columnList = "token_family_id")
        }
)
public class AuthSession extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_family_id", nullable = false)
    private UUID tokenFamilyId;

    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason", length = 30)
    private RevocationReason revocationReason;

    @Column(name = "replaced_by_session_id")
    private UUID replacedBySessionId;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** Required by JPA. Not intended for application use. */
    protected AuthSession() {
        super();
    }

    private AuthSession(Builder builder) {
        super(builder.id);
        this.userId = builder.userId;
        this.tokenFamilyId = builder.tokenFamilyId;
        this.refreshTokenHash = builder.refreshTokenHash;
        this.issuedAt = builder.issuedAt;
        this.expiresAt = builder.expiresAt;
        this.lastUsedAt = builder.lastUsedAt;
        this.revokedAt = builder.revokedAt;
        this.revocationReason = builder.revocationReason;
        this.replacedBySessionId = builder.replacedBySessionId;
        this.ipHash = builder.ipHash;
        this.userAgent = builder.userAgent;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTokenFamilyId() {
        return tokenFamilyId;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public RevocationReason getRevocationReason() {
        return revocationReason;
    }

    public UUID getReplacedBySessionId() {
        return replacedBySessionId;
    }

    public String getIpHash() {
        return ipHash;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant asOf) {
        return expiresAt.isBefore(asOf);
    }

    public boolean isActive(Instant asOf) {
        return !isRevoked() && !isExpired(asOf);
    }

    /** Revokes this session while preserving the token-family history. */
    public boolean revoke(RevocationReason reason, Instant at) {
        if (isRevoked() || reason == null || at == null) {
            return false;
        }
        this.revokedAt = at;
        this.revocationReason = reason;
        return true;
    }

    /** Marks this token as normally rotated to a replacement session. */
    public boolean markRotated(UUID replacementSessionId, Instant at) {
        if (isRevoked() || replacementSessionId == null || at == null) {
            return false;
        }
        this.revokedAt = at;
        this.revocationReason = RevocationReason.ROTATED;
        this.replacedBySessionId = replacementSessionId;
        this.lastUsedAt = at;
        return true;
    }

    /**
     * True when this session was created before the account's credentials
     * changed, in which case it must not be allowed to refresh.
     */
    public boolean predatesCredentialChange(Instant credentialsChangedAt) {
        return credentialsChangedAt != null && issuedAt.isBefore(credentialsChangedAt);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuthSession session)) {
            return false;
        }
        return getId() != null && getId().equals(session.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Deliberately omits the token hash. */
    @Override
    public String toString() {
        return "AuthSession{"
                + "id=" + getId()
                + ", userId=" + userId
                + ", family=" + tokenFamilyId
                + ", revoked=" + isRevoked()
                + '}';
    }

    public static class Builder {

        private UUID id;
        private UUID userId;
        private UUID tokenFamilyId;
        private String refreshTokenHash;
        private Instant issuedAt;
        private Instant expiresAt;
        private Instant lastUsedAt;
        private Instant revokedAt;
        private RevocationReason revocationReason;
        private UUID replacedBySessionId;
        private String ipHash;
        private String userAgent;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder setTokenFamilyId(UUID tokenFamilyId) {
            this.tokenFamilyId = tokenFamilyId;
            return this;
        }

        public Builder setRefreshTokenHash(String refreshTokenHash) {
            this.refreshTokenHash = refreshTokenHash;
            return this;
        }

        public Builder setIssuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder setLastUsedAt(Instant lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder setRevokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder setRevocationReason(RevocationReason revocationReason) {
            this.revocationReason = revocationReason;
            return this;
        }

        public Builder setReplacedBySessionId(UUID replacedBySessionId) {
            this.replacedBySessionId = replacedBySessionId;
            return this;
        }

        public Builder setIpHash(String ipHash) {
            this.ipHash = ipHash;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder copy(AuthSession session) {
            this.id = session.getId();
            this.userId = session.getUserId();
            this.tokenFamilyId = session.getTokenFamilyId();
            this.refreshTokenHash = session.getRefreshTokenHash();
            this.issuedAt = session.getIssuedAt();
            this.expiresAt = session.getExpiresAt();
            this.lastUsedAt = session.getLastUsedAt();
            this.revokedAt = session.getRevokedAt();
            this.revocationReason = session.getRevocationReason();
            this.replacedBySessionId = session.getReplacedBySessionId();
            this.ipHash = session.getIpHash();
            this.userAgent = session.getUserAgent();
            return this;
        }

        public AuthSession build() {
            return new AuthSession(this);
        }
    }
}
