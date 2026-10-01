package com.example.unimarket.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates and hashes opaque tokens.
 *
 * <p>Refresh tokens and one-time tokens are stored only as SHA-256 hashes, so a
 * database disclosure yields nothing replayable. SHA-256 is appropriate here,
 * unlike for passwords, because these tokens are long random values rather than
 * low-entropy human input, so they are not vulnerable to brute force.
 */
public final class TokenHasher {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_TOKEN_BYTES = 32;

    private TokenHasher() {
        // Utility class.
    }

    /**
     * Generates a URL-safe random token with 256 bits of entropy.
     */
    public static String generateToken() {
        byte[] bytes = new byte[DEFAULT_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns the lowercase hexadecimal SHA-256 hash of the supplied token.
     *
     * @return the 64-character hash, or {@code null} when the input is blank
     */
    public static String hash(String rawToken) {
        if (Helper.isNullOrEmpty(rawToken)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required but unavailable", exception);
        }
    }

    /**
     * Hashes a value that is retained only as a privacy-preserving signal,
     * such as a client IP address.
     */
    public static String hashSignal(String value) {
        return Helper.isNullOrEmpty(value) ? null : hash(value);
    }
}
