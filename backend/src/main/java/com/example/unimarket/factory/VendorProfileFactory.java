package com.example.unimarket.factory;

import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.VendorType;
import com.example.unimarket.domain.enums.VendorVerificationStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class VendorProfileFactory {
    private VendorProfileFactory() { }

    public static VendorProfile create(UUID userId, VendorType type, String businessName,
                                       String description, String registrationNumber, String websiteUrl) {
        Values values = values(type, businessName, description, registrationNumber, websiteUrl);
        if (userId == null || values == null) return null;
        return new VendorProfile.Builder().setId(Helper.generateId()).setUserId(userId)
                .setVendorType(type).setBusinessName(values.name).setDescription(values.description)
                .setRegistrationNumber(values.registration).setWebsiteUrl(values.website)
                .setVerificationStatus(VendorVerificationStatus.PENDING)
                .setSubmittedAt(Instant.now()).build();
    }

    public static VendorProfile resubmit(VendorProfile profile, VendorType type, String businessName,
                                         String description, String registrationNumber, String websiteUrl) {
        Values values = values(type, businessName, description, registrationNumber, websiteUrl);
        return profile != null && values != null && profile.resubmit(type, values.name, values.description,
                values.registration, values.website, Instant.now()) ? profile : null;
    }

    public static VendorProfile review(VendorProfile profile, VendorVerificationStatus status,
                                       UUID reviewerId, String note) {
        String cleanedNote = Helper.cleanText(note);
        return profile != null && profile.review(status, reviewerId, cleanedNote, Instant.now()) ? profile : null;
    }

    private static Values values(VendorType type, String name, String description,
                                 String registration, String website) {
        String cleanName = Helper.cleanText(name);
        String cleanDescription = Helper.cleanText(description);
        String cleanRegistration = Helper.cleanText(registration);
        String cleanWebsite = Helper.cleanText(website);
        if (type == null || cleanName == null || cleanDescription == null
                || cleanName.length() > 160 || cleanDescription.length() > 1000
                || cleanRegistration != null && cleanRegistration.length() > 80
                || cleanWebsite != null && cleanWebsite.length() > 500
                || type == VendorType.REGISTERED_BUSINESS && cleanRegistration == null) return null;
        return new Values(cleanName, cleanDescription, cleanRegistration, cleanWebsite);
    }

    private record Values(String name, String description, String registration, String website) { }
}
