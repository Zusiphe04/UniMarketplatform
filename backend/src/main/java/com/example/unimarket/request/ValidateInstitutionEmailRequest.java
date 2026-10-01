package com.example.unimarket.request;

import com.example.unimarket.domain.enums.CommunityPersona;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Preflights an academic email against the selected institution's evidenced policy. */
public record ValidateInstitutionEmailRequest(
        @NotBlank(message = "Institution is required")
        @Size(max = 160, message = "Institution must not exceed 160 characters")
        String organizationName,
        @NotNull(message = "Persona is required") CommunityPersona persona,
        @NotBlank(message = "Email address is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254, message = "Email address must not exceed 254 characters")
        String email
) { }
