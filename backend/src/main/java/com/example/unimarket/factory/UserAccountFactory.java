package com.example.unimarket.factory;

import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.util.Helper;

import java.time.Instant;

/**
 * Creates valid {@link UserAccount} aggregates.
 *
 * <p>The factory is the only place that decides whether an account may exist.
 * It normalises the email, refuses invalid input by returning {@code null},
 * and applies the lifecycle defaults for a newly registered member.
 *
 * <p>The Ecommerce-style flow activates a valid registration immediately; no
 * separate one-time verification token is created.
 *
 * <p>The factory never hashes a password itself. It accepts an already-hashed
 * value, which keeps the encoder in the service layer and makes it impossible
 * to store a plaintext password through this path by mistake.
 */
public final class UserAccountFactory {

    private UserAccountFactory() {
        // Factory class.
    }

    /**
     * Creates a newly registered, active account.
     *
     * <p>The supplied address is validated and stored immediately, matching the
     * simpler Ecommerce registration flow without a verification-link step.
     *
     * @param email        the raw email address, normalised by this method
     * @param passwordHash an already-encoded password hash
     * @return the new account, or {@code null} when the input is not valid
     */
    public static UserAccount createAccount(String email,
                                            String passwordHash,
                                            String termsVersion,
                                            String privacyVersion) {

        if (!Helper.isValidEmail(email)) {
            return null;
        }
        if (Helper.isNullOrEmpty(passwordHash)) {
            return null;
        }
        if (Helper.isNullOrEmpty(termsVersion) || Helper.isNullOrEmpty(privacyVersion)) {
            return null;
        }

        Instant now = Instant.now();

        return new UserAccount.Builder()
                .setId(Helper.generateId())
                .setEmail(Helper.normalizeEmail(email))
                .setPasswordHash(passwordHash)
                .setStatus(AccountStatus.ACTIVE)
                .setEmailVerifiedAt(now)
                .setFailedLoginCount(0)
                .setLockedUntil(null)
                .setCredentialsChangedAt(now)
                .setLastLoginAt(null)
                .setTermsVersion(termsVersion.trim())
                .setTermsAcceptedAt(now)
                .setPrivacyVersion(privacyVersion.trim())
                .setPrivacyAcceptedAt(now)
                .build();
    }

    /**
     * Returns a copy of the account marked as email-verified and active.
     *
     * @return the activated account, or {@code null} when the account is absent
     */
    public static UserAccount verifyEmail(UserAccount account) {
        if (account == null || !account.verifyEmail(Instant.now())) {
            return null;
        }
        return account;
    }

    /**
     * Returns a copy carrying a new password hash.
     *
     * <p>Advancing {@code credentialsChangedAt} is what allows the token
     * service to reject sessions that were issued before the change.
     *
     * @return the updated account, or {@code null} when the input is not valid
     */
    public static UserAccount changePassword(UserAccount account, String newPasswordHash) {
        if (account == null || Helper.isNullOrEmpty(newPasswordHash)) {
            return null;
        }
        account.changePasswordHash(newPasswordHash, Instant.now());
        return account;
    }

    /**
     * Returns a copy reflecting a successful authentication.
     */
    public static UserAccount recordSuccessfulLogin(UserAccount account) {
        if (account == null) {
            return null;
        }
        account.recordSuccessfulLogin(Instant.now());
        return account;
    }

    /**
     * Returns a copy with the failure counter incremented, applying a
     * temporary lockout once the threshold is reached.
     */
    public static UserAccount recordFailedLogin(UserAccount account,
                                                 int maxFailedAttempts,
                                                 java.time.Duration lockoutDuration) {
        if (account == null) {
            return null;
        }
        account.recordFailedLogin(maxFailedAttempts, lockoutDuration, Instant.now());
        return account;
    }

    /**
     * Returns a copy with the given status applied.
     *
     * @return the updated account, or {@code null} when the input is not valid
     */
    public static UserAccount changeStatus(UserAccount account, AccountStatus status) {
        if (account == null || status == null || !account.changeStatus(status)) {
            return null;
        }
        return account;
    }
}
