package com.example.unimarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Public and contact details for a member, kept separate from credentials.
 *
 * <p>The owning account is referenced by identifier rather than by a JPA
 * association. Crossing aggregate boundaries by id keeps the transaction
 * boundary explicit and avoids accidental lazy loading, which matters because
 * {@code spring.jpa.open-in-view} is disabled.
 */
@Entity
@Table(
        name = "user_profile",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_profile_user", columnNames = "user_id")
)
public class UserProfile extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

    @Column(name = "avatar_storage_key", length = 500)
    private String avatarStorageKey;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    /** Required by JPA. Not intended for application use. */
    protected UserProfile() {
        super();
    }

    private UserProfile(Builder builder) {
        super(builder.id);
        this.userId = builder.userId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.displayName = builder.displayName;
        this.phoneNumber = builder.phoneNumber;
        this.phoneVerifiedAt = builder.phoneVerifiedAt;
        this.avatarStorageKey = builder.avatarStorageKey;
        this.bio = builder.bio;
        this.preferredLanguage = builder.preferredLanguage;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Instant getPhoneVerifiedAt() {
        return phoneVerifiedAt;
    }

    public String getAvatarStorageKey() {
        return avatarStorageKey;
    }

    public String getBio() {
        return bio;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Applies validated presentation and contact changes to the managed profile. */
    public void updateDetails(String newDisplayName, String newPhoneNumber, String newBio) {
        if (newDisplayName != null) {
            this.displayName = newDisplayName;
        }
        if (!Objects.equals(this.phoneNumber, newPhoneNumber)) {
            this.phoneNumber = newPhoneNumber;
            this.phoneVerifiedAt = null;
        }
        this.bio = newBio;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserProfile profile)) {
            return false;
        }
        return getId() != null && getId().equals(profile.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserProfile{"
                + "id=" + getId()
                + ", userId=" + userId
                + ", displayName='" + displayName + '\''
                + '}';
    }

    public static class Builder {

        private UUID id;
        private UUID userId;
        private String firstName;
        private String lastName;
        private String displayName;
        private String phoneNumber;
        private Instant phoneVerifiedAt;
        private String avatarStorageKey;
        private String bio;
        private String preferredLanguage;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setPhoneVerifiedAt(Instant phoneVerifiedAt) {
            this.phoneVerifiedAt = phoneVerifiedAt;
            return this;
        }

        public Builder setAvatarStorageKey(String avatarStorageKey) {
            this.avatarStorageKey = avatarStorageKey;
            return this;
        }

        public Builder setBio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public Builder copy(UserProfile profile) {
            this.id = profile.getId();
            this.userId = profile.getUserId();
            this.firstName = profile.getFirstName();
            this.lastName = profile.getLastName();
            this.displayName = profile.getDisplayName();
            this.phoneNumber = profile.getPhoneNumber();
            this.phoneVerifiedAt = profile.getPhoneVerifiedAt();
            this.avatarStorageKey = profile.getAvatarStorageKey();
            this.bio = profile.getBio();
            this.preferredLanguage = profile.getPreferredLanguage();
            return this;
        }

        public UserProfile build() {
            return new UserProfile(this);
        }
    }
}
