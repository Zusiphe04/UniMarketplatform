package com.example.unimarket.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for account registration.
 *
 * <p>A record is used because a request is an immutable snapshot of what the
 * client sent. Constraints are declared here so that malformed input is
 * rejected at the edge, before any service or database work begins.
 *
 * <p>Note that {@code acceptedTerms} and {@code acceptedPrivacy} are required
 * to be {@code true} by the service rather than by an annotation, so the
 * refusal reason can be reported clearly.
 */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name must not exceed 80 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name must not exceed 80 characters")
        String lastName,

        @Size(max = 120, message = "Display name must not exceed 120 characters")
        String displayName,

        boolean acceptedTerms,

        boolean acceptedPrivacy
) {
}
