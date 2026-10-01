package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.VendorType;
import com.example.unimarket.domain.enums.VendorVerificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Seller eligibility record reviewed independently from authorization roles. */
@Entity
@Table(name = "vendor_profile",
        uniqueConstraints = @UniqueConstraint(name = "uk_vendor_profile_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_vendor_profile_status", columnList = "verification_status, submitted_at"))
public class VendorProfile extends AuditableEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_type", nullable = false, length = 30) private VendorType vendorType;
    @Column(name = "business_name", nullable = false, length = 160) private String businessName;
    @Column(name = "description", nullable = false, length = 1000) private String description;
    @Column(name = "registration_number", length = 80) private String registrationNumber;
    @Column(name = "website_url", length = 500) private String websiteUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VendorVerificationStatus verificationStatus;
    @Column(name = "submitted_at", nullable = false) private Instant submittedAt;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "reviewed_by_user_id") private UUID reviewedByUserId;
    @Column(name = "review_note", length = 500) private String reviewNote;

    protected VendorProfile() { super(); }
    private VendorProfile(Builder builder) {
        super(builder.id);
        userId = builder.userId;
        vendorType = builder.vendorType;
        businessName = builder.businessName;
        description = builder.description;
        registrationNumber = builder.registrationNumber;
        websiteUrl = builder.websiteUrl;
        verificationStatus = builder.verificationStatus;
        submittedAt = builder.submittedAt;
        reviewedAt = builder.reviewedAt;
        reviewedByUserId = builder.reviewedByUserId;
        reviewNote = builder.reviewNote;
    }

    public UUID getUserId() { return userId; }
    public VendorType getVendorType() { return vendorType; }
    public String getBusinessName() { return businessName; }
    public String getDescription() { return description; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getWebsiteUrl() { return websiteUrl; }
    public VendorVerificationStatus getVerificationStatus() { return verificationStatus; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public UUID getReviewedByUserId() { return reviewedByUserId; }
    public String getReviewNote() { return reviewNote; }

    public boolean resubmit(VendorType type, String name, String details,
                            String registration, String website, Instant at) {
        if (verificationStatus == VendorVerificationStatus.VERIFIED
                || verificationStatus == VendorVerificationStatus.SUSPENDED || at == null) return false;
        vendorType = type;
        businessName = name;
        description = details;
        registrationNumber = registration;
        websiteUrl = website;
        verificationStatus = VendorVerificationStatus.PENDING;
        submittedAt = at;
        reviewedAt = null;
        reviewedByUserId = null;
        reviewNote = null;
        return true;
    }

    public boolean review(VendorVerificationStatus status, UUID reviewerId, String note, Instant at) {
        if (status == null || status == VendorVerificationStatus.PENDING
                || reviewerId == null || at == null) return false;
        verificationStatus = status;
        reviewedByUserId = reviewerId;
        reviewNote = note;
        reviewedAt = at;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof VendorProfile value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private VendorType vendorType;
        private String businessName;
        private String description;
        private String registrationNumber;
        private String websiteUrl;
        private VendorVerificationStatus verificationStatus;
        private Instant submittedAt;
        private Instant reviewedAt;
        private UUID reviewedByUserId;
        private String reviewNote;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setUserId(UUID value) { userId = value; return this; }
        public Builder setVendorType(VendorType value) { vendorType = value; return this; }
        public Builder setBusinessName(String value) { businessName = value; return this; }
        public Builder setDescription(String value) { description = value; return this; }
        public Builder setRegistrationNumber(String value) { registrationNumber = value; return this; }
        public Builder setWebsiteUrl(String value) { websiteUrl = value; return this; }
        public Builder setVerificationStatus(VendorVerificationStatus value) { verificationStatus = value; return this; }
        public Builder setSubmittedAt(Instant value) { submittedAt = value; return this; }
        public Builder setReviewedAt(Instant value) { reviewedAt = value; return this; }
        public Builder setReviewedByUserId(UUID value) { reviewedByUserId = value; return this; }
        public Builder setReviewNote(String value) { reviewNote = value; return this; }
        public Builder copy(VendorProfile value) {
            return setId(value.getId()).setUserId(value.getUserId()).setVendorType(value.getVendorType())
                    .setBusinessName(value.getBusinessName()).setDescription(value.getDescription())
                    .setRegistrationNumber(value.getRegistrationNumber()).setWebsiteUrl(value.getWebsiteUrl())
                    .setVerificationStatus(value.getVerificationStatus()).setSubmittedAt(value.getSubmittedAt())
                    .setReviewedAt(value.getReviewedAt()).setReviewedByUserId(value.getReviewedByUserId())
                    .setReviewNote(value.getReviewNote());
        }
        public VendorProfile build() { return new VendorProfile(this); }
    }
}
