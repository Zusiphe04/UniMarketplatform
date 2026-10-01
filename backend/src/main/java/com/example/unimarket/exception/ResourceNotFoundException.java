package com.example.unimarket.exception;

/**
 * Thrown when a requested resource does not exist, or when revealing that it
 * exists would itself disclose information the caller should not have.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resourceName) {
        return new ResourceNotFoundException(resourceName + " was not found.");
    }
}
