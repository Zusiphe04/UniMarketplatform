package com.example.unimarket.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for an authenticated password change.
 *
 * <p>The current password is required so that a hijacked access token cannot
 * be used on its own to lock the rightful owner out of the account.
 */
public record ChangePasswordRequest(

        @NotBlank(message = "Current password is required")
        @Size(max = 128)
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
        String newPassword
) {
}
