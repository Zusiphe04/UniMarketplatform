package com.example.unimarket.domain.enums;

/** Why an {@link com.example.unimarket.domain.AuthSession} stopped being valid. */
public enum RevocationReason {
    LOGOUT,
    LOGOUT_ALL,
    /** Superseded by a newer token during normal rotation. */
    ROTATED,
    /** An already-rotated token was presented again, so the family was revoked. */
    REUSE_DETECTED,
    PASSWORD_CHANGE,
    ADMIN_ACTION,
    /** A role was removed, so authorities embedded in issued tokens are stale. */
    ROLE_REVOKED,
    EXPIRED
}
