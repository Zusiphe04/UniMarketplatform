package com.example.unimarket.exception;

/**
 * Thrown when a request is syntactically valid but breaks a business rule that
 * annotations cannot express, such as declining the terms of use.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
