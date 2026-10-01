package com.example.unimarket.domain.enums;

/**
 * Lifecycle state of a {@link UserAccount}.
 *
 * <p>Only {@link #ACTIVE} accounts may authenticate. An account is never
 * deleted outright, because credentials, orders, and audit history depend
 * on it remaining resolvable.
 */
public enum AccountStatus {

    /** Registered but the email address has not been verified yet. */
    PENDING_EMAIL,

    /** Verified and permitted to authenticate. */
    ACTIVE,

    /** Temporarily locked after repeated failed authentication attempts. */
    LOCKED,

    /** Suspended by a moderator or administrator. */
    SUSPENDED,

    /** Closed by the member. Retained for historical references. */
    CLOSED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }

    /**
     * Explicit account lifecycle matrix. CLOSED is terminal; email verification
     * is the only normal path from PENDING_EMAIL to ACTIVE.
     */
    public boolean canTransitionTo(AccountStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case PENDING_EMAIL -> target == SUSPENDED || target == CLOSED;
            case ACTIVE -> target == SUSPENDED || target == CLOSED;
            case LOCKED -> target == ACTIVE || target == SUSPENDED || target == CLOSED;
            case SUSPENDED -> target == ACTIVE || target == CLOSED;
            case CLOSED -> false;
        };
    }
}
