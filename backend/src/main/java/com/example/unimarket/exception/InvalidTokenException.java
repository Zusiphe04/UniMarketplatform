package com.example.unimarket.exception;

/** Thrown when an opaque refresh token cannot be honoured. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public static InvalidTokenException refreshToken() {
        return new InvalidTokenException("Your session has expired. Please sign in again.");
    }
}
