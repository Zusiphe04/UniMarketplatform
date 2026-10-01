package com.example.unimarket.response;

import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.VendorType;
import com.example.unimarket.domain.enums.VendorVerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record VendorProfileResponse(
        UUID id,
        UUID userId,
        VendorType vendorType,
        String businessName,
        String description,
        String registrationNumber,
        String websiteUrl,
        VendorVerificationStatus verificationStatus,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewNote
) {
    public static VendorProfileResponse from(VendorProfile value) {
        return new VendorProfileResponse(value.getId(), value.getUserId(), value.getVendorType(),
                value.getBusinessName(), value.getDescription(), value.getRegistrationNumber(),
                value.getWebsiteUrl(), value.getVerificationStatus(), value.getSubmittedAt(),
                value.getReviewedAt(), value.getReviewNote());
    }
}
