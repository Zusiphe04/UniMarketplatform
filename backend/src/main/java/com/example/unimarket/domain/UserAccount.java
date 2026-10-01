package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.AccountStatus;

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
 * Authentication root for every person using UniMarket.
 *
 * <p>Constructed exclusively through {@link Builder}, which keeps the entity
 * immutable from the outside: there are no public setters, so an account
 * cannot be left in a half-populated state by calling code.
 *
 * <p>The stored email is always normalised, and the password is held only as
 * a hash. Neither the hash nor any token value is ever exposed by
 * {@link #toString()}.
 */
@Entity
@Table(
        name = "user_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_account_email", columnNames = "email"),
        indexes = @Index(name = "idx_user_account_status", columnList = "status, locked_until")
)
public class UserAccount extends AuditableEntity {

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AccountStatus status;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "credentials_changed_at", nullable = false)
    private Instant credentialsChangedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "terms_version", nullable = false, length = 30)
    private String termsVersion;

    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Column(name = "privacy_version", nullable = false, length = 30)
    private String privacyVersion;

    @Column(name = "privacy_accepted_at", nullable = false)
    private Instant privacyAcceptedAt;

    /** Required by JPA. Not intended for application use. */
    protected UserAccount() {
        super();
    }

    private UserAccount(Builder builder) {
        super(builder.id);
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.status = builder.status;
        this.emailVerifiedAt = builder.emailVerifiedAt;
        this.failedLoginCount = builder.failedLoginCount;
        this.lockedUntil = builder.lockedUntil;
        this.credentialsChangedAt = builder.credentialsChangedAt;
        this.lastLoginAt = builder.lastLoginAt;
        this.termsVersion = builder.termsVersion;
        this.termsAcceptedAt = builder.termsAcceptedAt;
        this.privacyVersion = builder.privacyVersion;
        this.privacyAcceptedAt = builder.privacyAcceptedAt;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getCredentialsChangedAt() {
        return credentialsChangedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public Instant getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public String getPrivacyVersion() {
        return privacyVersion;
    }

    public Instant getPrivacyAcceptedAt() {
        return privacyAcceptedAt;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    /** True while a temporary lockout is still in effect. */
    public boolean isCurrentlyLocked(Instant asOf) {
        return lockedUntil != null && lockedUntil.isAfter(asOf);
    }

    /** An account may authenticate only when active, verified, and not locked out. */
    public boolean canAuthenticate(Instant asOf) {
        return status.canAuthenticate() && isEmailVerified() && !isCurrentlyLocked(asOf);
    }

    /**
     * Marks a pending account as verified. Verification is deliberately legal
     * only from PENDING_EMAIL, so an old link cannot reactivate a suspended or
     * closed account.
     */
    public boolean verifyEmail(Instant verifiedAt) {
        if (status != AccountStatus.PENDING_EMAIL || verifiedAt == null) {
            return false;
        }
        this.emailVerifiedAt = verifiedAt;
        this.status = AccountStatus.ACTIVE;
        return true;
    }

    /** Updates the password hash while invalidating sessions issued earlier. */
    public void changePasswordHash(String newPasswordHash, Instant changedAt) {
        this.passwordHash = newPasswordHash;
        this.credentialsChangedAt = changedAt;
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /** Records a successful login and clears temporary failure state. */
    public void recordSuccessfulLogin(Instant loggedInAt) {
        this.lastLoginAt = loggedInAt;
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /**
     * Records a failed login. Account status remains ACTIVE: lockout is a
     * temporary time window, not a permanent lifecycle state.
     */
    public void recordFailedLogin(int maxFailedAttempts, java.time.Duration lockoutDuration, Instant now) {
        this.failedLoginCount++;
        if (maxFailedAttempts > 0
                && this.failedLoginCount >= maxFailedAttempts
                && lockoutDuration != null
                && !lockoutDuration.isNegative()) {
            this.lockedUntil = now.plus(lockoutDuration);
        }
    }

    /** Applies an administrator-approved lifecycle transition. */
    public boolean changeStatus(AccountStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            return false;
        }
        this.status = newStatus;
        if (newStatus == AccountStatus.ACTIVE) {
            this.failedLoginCount = 0;
            this.lockedUntil = null;
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserAccount account)) {
            return false;
        }
        return getId() != null && getId().equals(account.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Deliberately omits the password hash so credentials cannot leak into logs. */
    @Override
    public String toString() {
        return "UserAccount{"
                + "id=" + getId()
                + ", email='" + email + '\''
                + ", status=" + status
                + ", emailVerified=" + isEmailVerified()
                + '}';
    }

    /**
     * Builder for {@link UserAccount}.
     *
     * <p>Values are assembled here and validated by the factory package before
     * {@link #build()} is called.
     */
    public static class Builder {

        private UUID id;
        private String email;
        private String passwordHash;
        private AccountStatus status;
        private Instant emailVerifiedAt;
        private int failedLoginCount;
        private Instant lockedUntil;
        private Instant credentialsChangedAt;
        private Instant lastLoginAt;
        private String termsVersion;
        private Instant termsAcceptedAt;
        private String privacyVersion;
        private Instant privacyAcceptedAt;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder setStatus(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Builder setEmailVerifiedAt(Instant emailVerifiedAt) {
            this.emailVerifiedAt = emailVerifiedAt;
            return this;
        }

        public Builder setFailedLoginCount(int failedLoginCount) {
            this.failedLoginCount = failedLoginCount;
            return this;
        }

        public Builder setLockedUntil(Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }

        public Builder setCredentialsChangedAt(Instant credentialsChangedAt) {
            this.credentialsChangedAt = credentialsChangedAt;
            return this;
        }

        public Builder setLastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder setTermsVersion(String termsVersion) {
            this.termsVersion = termsVersion;
            return this;
        }

        public Builder setTermsAcceptedAt(Instant termsAcceptedAt) {
            this.termsAcceptedAt = termsAcceptedAt;
            return this;
        }

        public Builder setPrivacyVersion(String privacyVersion) {
            this.privacyVersion = privacyVersion;
            return this;
        }

        public Builder setPrivacyAcceptedAt(Instant privacyAcceptedAt) {
            this.privacyAcceptedAt = privacyAcceptedAt;
            return this;
        }

        /**
         * Copies every value from an existing account, so an update can change
         * one field without rebuilding the rest by hand.
         */
        public Builder copy(UserAccount account) {
            this.id = account.getId();
            this.email = account.getEmail();
            this.passwordHash = account.getPasswordHash();
            this.status = account.getStatus();
            this.emailVerifiedAt = account.getEmailVerifiedAt();
            this.failedLoginCount = account.getFailedLoginCount();
            this.lockedUntil = account.getLockedUntil();
            this.credentialsChangedAt = account.getCredentialsChangedAt();
            this.lastLoginAt = account.getLastLoginAt();
            this.termsVersion = account.getTermsVersion();
            this.termsAcceptedAt = account.getTermsAcceptedAt();
            this.privacyVersion = account.getPrivacyVersion();
            this.privacyAcceptedAt = account.getPrivacyAcceptedAt();
            return this;
        }

        public UserAccount build() {
            return new UserAccount(this);
        }
    }
}
