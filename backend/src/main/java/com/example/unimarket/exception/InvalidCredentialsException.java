package com.example.unimarket.exception;

/**
 * Thrown for every failed authentication, whatever the underlying cause.
 *
 * <p>Wrong password, unknown address, unverified email, lockout, and suspension
 * all raise this same exception with the same message. Distinguishing them in
 * the response would tell an attacker which addresses are registered and which
 * accounts are worth targeting.
 */
public class InvalidCredentialsException extends RuntimeException {

    private static final String GENERIC_MESSAGE = "Invalid email or password.";

    public InvalidCredentialsException() {
        super(GENERIC_MESSAGE);
    }
}
