package com.example.unimarket.response;

import java.util.UUID;

/** Frontend contract returned after an account is created and activated. */
public record RegistrationResponse(UUID accountId, String message) {
    public static RegistrationResponse registered(UUID accountId) {
        return new RegistrationResponse(accountId, "Account registered successfully. You can now sign in.");
    }
}
