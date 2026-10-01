package com.example.unimarket.response;

import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.AcademicQualification;
import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.PersonaVerificationStatus;

import java.time.Instant;
import java.util.UUID;

/** Frontend-safe persona and affiliation view. */
public record PersonaResponse(
        UUID id,
        CommunityPersona persona,
        PersonaVerificationStatus verificationStatus,
        String organizationName,
        AcademicQualification qualification,
        Integer yearOfStudy,
        String verifiedEmailDomain,
        Instant assignedAt,
        Instant verifiedAt
) {
    public static PersonaResponse from(UserPersonaAssignment value) {
        return new PersonaResponse(value.getId(), value.getPersona(), value.getVerificationStatus(),
                value.getOrganizationName(), value.getQualification(), value.getYearOfStudy(),
                value.getVerifiedEmailDomain(), value.getAssignedAt(), value.getVerifiedAt());
    }
}
