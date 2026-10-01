package com.example.unimarket.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for authentication.
 *
 * <p>The email is deliberately not annotated with {@code @Email}. A malformed
 * address on login must produce the same generic failure as a wrong password,
 * otherwise the validation response itself would reveal which addresses are
 * plausible.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Size(max = 254)
        String email,

        @NotBlank(message = "Password is required")
        @Size(max = 128)
        String password
) {
}
