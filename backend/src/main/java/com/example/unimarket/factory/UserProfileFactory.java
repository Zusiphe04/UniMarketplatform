package com.example.unimarket.factory;

import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.util.Helper;

import java.util.UUID;

/**
 * Creates valid {@link UserProfile} aggregates.
 */
public final class UserProfileFactory {

    private static final String DEFAULT_LANGUAGE = "en-ZA";

    private UserProfileFactory() {
        // Factory class.
    }

    /**
     * Creates a profile for a newly registered account.
     *
     * <p>When no display name is supplied, the member's first name is used, so
     * the marketplace always has something to show without exposing a full
     * legal name by default.
     *
     * @return the new profile, or {@code null} when the input is not valid
     */
    public static UserProfile createProfile(UUID userId,
                                           String firstName,
                                           String lastName,
                                           String displayName) {

        if (userId == null) {
            return null;
        }

        String cleanedFirstName = Helper.cleanText(firstName);
        String cleanedLastName = Helper.cleanText(lastName);

        if (cleanedFirstName == null || cleanedLastName == null) {
            return null;
        }

        String resolvedDisplayName = Helper.cleanText(displayName);
        if (resolvedDisplayName == null) {
            resolvedDisplayName = cleanedFirstName;
        }

        return new UserProfile.Builder()
                .setId(Helper.generateId())
                .setUserId(userId)
                .setFirstName(cleanedFirstName)
                .setLastName(cleanedLastName)
                .setDisplayName(resolvedDisplayName)
                .setPreferredLanguage(DEFAULT_LANGUAGE)
                .build();
    }

    /**
     * Returns a copy with contact and presentation details updated.
     *
     * <p>A blank phone number clears the stored value; an invalid one is
     * rejected outright rather than silently ignored.
     *
     * @return the updated profile, or {@code null} when the input is not valid
     */
    public static UserProfile updateDetails(UserProfile profile,
                                            String displayName,
                                            String phoneNumber,
                                            String bio) {

        if (profile == null) {
            return null;
        }

        String cleanedDisplayName = Helper.cleanText(displayName);
        String resolvedPhone = profile.getPhoneNumber();
        if (phoneNumber != null) {
            if (Helper.isNullOrEmpty(phoneNumber)) {
                resolvedPhone = null;
            } else if (Helper.isValidPhoneNumber(phoneNumber)) {
                resolvedPhone = phoneNumber.trim();
            } else {
                return null;
            }
        }

        String resolvedBio = profile.getBio();
        if (bio != null) {
            resolvedBio = Helper.isNullOrEmpty(bio) ? null : Helper.cleanText(bio);
        }

        profile.updateDetails(cleanedDisplayName, resolvedPhone, resolvedBio);
        return profile;
    }
}
