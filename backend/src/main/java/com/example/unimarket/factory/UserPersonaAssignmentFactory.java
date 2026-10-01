package com.example.unimarket.factory;

import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.AcademicQualification;
import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.PersonaVerificationStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

/** Creates persona declarations and records trusted verification. */
public final class UserPersonaAssignmentFactory {
    private UserPersonaAssignmentFactory() { }

    public static UserPersonaAssignment declare(UUID userId,
                                                 CommunityPersona persona,
                                                 String organizationName,
                                                 String verifiedEmailDomain) {
        return declare(userId, persona, organizationName, null, null, verifiedEmailDomain);
    }

    public static UserPersonaAssignment declare(UUID userId,
                                                 CommunityPersona persona,
                                                 String organizationName,
                                                 AcademicQualification qualification,
                                                 Integer yearOfStudy,
                                                 String verifiedEmailDomain) {
        if (userId == null || persona == null || yearOfStudy != null && (yearOfStudy < 1 || yearOfStudy > 6)) {
            return null;
        }
        String organization = Helper.cleanText(organizationName);
        String domain = Helper.cleanText(verifiedEmailDomain);
        Instant now = Instant.now();
        boolean domainVerified = domain != null;
        return new UserPersonaAssignment.Builder()
                .setId(Helper.generateId())
                .setUserId(userId)
                .setPersona(persona)
                .setOrganizationName(organization)
                .setQualification(qualification)
                .setYearOfStudy(yearOfStudy)
                .setVerifiedEmailDomain(domain)
                .setVerificationStatus(domainVerified
                        ? PersonaVerificationStatus.VERIFIED
                        : PersonaVerificationStatus.DECLARED)
                .setAssignedAt(now)
                .setVerifiedAt(domainVerified ? now : null)
                .build();
    }

    public static UserPersonaAssignment verify(UserPersonaAssignment assignment, UUID actorId) {
        return assignment != null && assignment.verify(actorId, Instant.now()) ? assignment : null;
    }
}
