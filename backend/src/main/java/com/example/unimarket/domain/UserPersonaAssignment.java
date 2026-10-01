package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.AcademicQualification;
import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.PersonaVerificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Community identity kept separate from authorization roles. */
@Entity
@Table(name = "user_persona_assignment",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_persona", columnNames = {"user_id", "persona"}))
public class UserPersonaAssignment extends AuditableEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "persona", nullable = false, length = 30) private CommunityPersona persona;
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private PersonaVerificationStatus verificationStatus;
    @Column(name = "organization_name", length = 160) private String organizationName;
    @Enumerated(EnumType.STRING)
    @Column(name = "qualification", length = 40) private AcademicQualification qualification;
    @Column(name = "year_of_study") private Integer yearOfStudy;
    @Column(name = "verified_email_domain", length = 160) private String verifiedEmailDomain;
    @Column(name = "assigned_at", nullable = false) private Instant assignedAt;
    @Column(name = "verified_at") private Instant verifiedAt;
    @Column(name = "verified_by_user_id") private UUID verifiedByUserId;

    protected UserPersonaAssignment() { super(); }
    private UserPersonaAssignment(Builder builder) {
        super(builder.id);
        userId = builder.userId;
        persona = builder.persona;
        verificationStatus = builder.verificationStatus;
        organizationName = builder.organizationName;
        qualification = builder.qualification;
        yearOfStudy = builder.yearOfStudy;
        verifiedEmailDomain = builder.verifiedEmailDomain;
        assignedAt = builder.assignedAt;
        verifiedAt = builder.verifiedAt;
        verifiedByUserId = builder.verifiedByUserId;
    }

    public UUID getUserId() { return userId; }
    public CommunityPersona getPersona() { return persona; }
    public PersonaVerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getOrganizationName() { return organizationName; }
    public AcademicQualification getQualification() { return qualification; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public String getVerifiedEmailDomain() { return verifiedEmailDomain; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public UUID getVerifiedByUserId() { return verifiedByUserId; }
    public boolean isVerified() { return verificationStatus == PersonaVerificationStatus.VERIFIED; }

    public boolean verify(UUID actorId, Instant at) {
        if (actorId == null || at == null || isVerified()) return false;
        verificationStatus = PersonaVerificationStatus.VERIFIED;
        verifiedAt = at;
        verifiedByUserId = actorId;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof UserPersonaAssignment value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    @Override public String toString() {
        return "UserPersonaAssignment{userId=" + userId + ", persona=" + persona
                + ", status=" + verificationStatus + '}';
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private CommunityPersona persona;
        private PersonaVerificationStatus verificationStatus;
        private String organizationName;
        private AcademicQualification qualification;
        private Integer yearOfStudy;
        private String verifiedEmailDomain;
        private Instant assignedAt;
        private Instant verifiedAt;
        private UUID verifiedByUserId;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setUserId(UUID value) { userId = value; return this; }
        public Builder setPersona(CommunityPersona value) { persona = value; return this; }
        public Builder setVerificationStatus(PersonaVerificationStatus value) { verificationStatus = value; return this; }
        public Builder setOrganizationName(String value) { organizationName = value; return this; }
        public Builder setQualification(AcademicQualification value) { qualification = value; return this; }
        public Builder setYearOfStudy(Integer value) { yearOfStudy = value; return this; }
        public Builder setVerifiedEmailDomain(String value) { verifiedEmailDomain = value; return this; }
        public Builder setAssignedAt(Instant value) { assignedAt = value; return this; }
        public Builder setVerifiedAt(Instant value) { verifiedAt = value; return this; }
        public Builder setVerifiedByUserId(UUID value) { verifiedByUserId = value; return this; }
        public Builder copy(UserPersonaAssignment value) {
            return setId(value.getId()).setUserId(value.getUserId()).setPersona(value.getPersona())
                    .setVerificationStatus(value.getVerificationStatus())
                    .setOrganizationName(value.getOrganizationName())
                    .setQualification(value.getQualification()).setYearOfStudy(value.getYearOfStudy())
                    .setVerifiedEmailDomain(value.getVerifiedEmailDomain())
                    .setAssignedAt(value.getAssignedAt()).setVerifiedAt(value.getVerifiedAt())
                    .setVerifiedByUserId(value.getVerifiedByUserId());
        }
        public UserPersonaAssignment build() { return new UserPersonaAssignment(this); }
    }
}
