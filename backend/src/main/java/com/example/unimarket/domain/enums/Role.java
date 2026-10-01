package com.example.unimarket.domain.enums;

/**
 * Authorization roles.
 *
 * <p>Roles describe what an account may do. They are deliberately separate
 * from trust badges such as a verified student or verified business, so that
 * one account can be both a buyer and a seller, and so that a self-declared
 * label can never grant a privilege.
 */
public enum Role {

    /** Granted automatically once an account becomes active. */
    BUYER,

    /** Granted after a seller profile is accepted. */
    SELLER,

    /** Reviews reports, content, and verification cases. Never self-assigned. */
    MODERATOR,

    /** Manages platform configuration, institutions, and roles. Never self-assigned. */
    ADMIN;

    /** Spring Security expects authorities to carry the {@code ROLE_} prefix. */
    public String asAuthority() {
        return "ROLE_" + name();
    }

    public boolean isPrivileged() {
        return this == MODERATOR || this == ADMIN;
    }
}
