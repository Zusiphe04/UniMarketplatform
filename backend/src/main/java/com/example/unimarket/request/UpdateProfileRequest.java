package com.example.unimarket.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for updating a member's own profile.
 *
 * <p>Every field is optional. A {@code null} field leaves the stored value
 * untouched, while an empty phone number clears it, so a member can remove a
 * number without deleting the whole profile.
 */
public record UpdateProfileRequest(

        @Size(max = 120, message = "Display name must not exceed 120 characters")
        String displayName,

        @Pattern(regexp = "^$|^\\+[1-9]\\d{7,14}$",
                message = "Phone number must be in international format, for example +27821234567")
        String phoneNumber,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio
) {
}
