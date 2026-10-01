package com.example.unimarket.response;

import com.example.unimarket.domain.UserProfile;

import java.util.UUID;

/**
 * Safe view of a member's profile.
 *
 * <p>The avatar storage key is exposed rather than a filesystem path, so no
 * internal storage layout is revealed.
 */
public record UserProfileResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String displayName,
        String phoneNumber,
        boolean phoneVerified,
        String bio,
        String avatarStorageKey,
        String preferredLanguage
) {

    public static UserProfileResponse from(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        return new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDisplayName(),
                profile.getPhoneNumber(),
                profile.getPhoneVerifiedAt() != null,
                profile.getBio(),
                profile.getAvatarStorageKey(),
                profile.getPreferredLanguage()
        );
    }
}
