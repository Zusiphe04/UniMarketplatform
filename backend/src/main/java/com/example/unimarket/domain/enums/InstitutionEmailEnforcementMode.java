package com.example.unimarket.domain.enums;

/** How an evidenced institution mailbox rule evaluates the email local part. */
public enum InstitutionEmailEnforcementMode {
    DOMAIN_ONLY,
    ADVISORY_PATTERN,
    STRICT_PATTERN,
    MANUAL_REVIEW
}
