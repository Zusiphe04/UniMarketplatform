package com.example.unimarket.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Shared validation and normalisation helpers used by the factory package.
 *
 * <p>Validation lives here rather than inside the builders so that a builder
 * stays a pure assembly mechanism, while the factories remain the single
 * place where an invalid aggregate is refused.
 */
public final class Helper {

    /** Deliberately permissive: mailbox existence is proven by sending a token, not by a regex. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    private static final Pattern E164_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public static final int PASSWORD_MIN_LENGTH = 12;
    public static final int PASSWORD_MAX_LENGTH = 128;

    private Helper() {
        // Utility class.
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Normalises an email address for storage and lookup by trimming
     * surrounding whitespace and lowercasing it.
     *
     * @return the normalised address, or {@code null} if the input was blank
     */
    public static String normalizeEmail(String email) {
        if (isNullOrEmpty(email)) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        String normalized = normalizeEmail(email);
        return normalized.length() <= 254 && EMAIL_PATTERN.matcher(normalized).matches();
    }

    /**
     * Extracts the domain portion of an email address, lowercased.
     *
     * @return the domain, or {@code null} when the address is not usable
     */
    public static String extractEmailDomain(String email) {
        if (!isValidEmail(email)) {
            return null;
        }
        String normalized = normalizeEmail(email);
        return normalized.substring(normalized.indexOf('@') + 1);
    }

    /**
     * Accepts passphrases and Unicode, and enforces length only.
     * Arbitrary character-composition rules are intentionally not applied.
     */
    public static boolean isValidPassword(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        int length = rawPassword.length();
        return length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return !isNullOrEmpty(phoneNumber) && E164_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    public static UUID generateId() {
        return UUID.randomUUID();
    }

    public static boolean isValidUuid(String candidate) {
        if (isNullOrEmpty(candidate)) {
            return false;
        }
        try {
            UUID.fromString(candidate.trim());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /** Collapses internal whitespace and trims, for names and titles. */
    public static String cleanText(String value) {
        if (isNullOrEmpty(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s{2,}", " ");
    }
}
