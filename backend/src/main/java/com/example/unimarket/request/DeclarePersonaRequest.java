package com.example.unimarket.request;

import com.example.unimarket.domain.enums.AcademicQualification;
import com.example.unimarket.domain.enums.CommunityPersona;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Declares a community identity; the server determines verification. */
public record DeclarePersonaRequest(
        @NotNull(message = "Persona is required") CommunityPersona persona,
        @Size(max = 160, message = "Organization name must not exceed 160 characters")
        String organizationName,
        AcademicQualification qualification,
        @Min(value = 1, message = "Year of study must be between 1 and 6")
        @Max(value = 6, message = "Year of study must be between 1 and 6")
        Integer yearOfStudy
) { }
