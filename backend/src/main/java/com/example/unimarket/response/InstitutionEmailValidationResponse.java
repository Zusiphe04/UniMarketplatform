package com.example.unimarket.response;

import com.example.unimarket.domain.enums.CommunityPersona;

/** Successful institution-email policy decision used by preflight and persona creation. */
public record InstitutionEmailValidationResponse(
        boolean valid,
        String institutionCode,
        String institutionName,
        CommunityPersona persona,
        String matchedDomain,
        String formatExample
) { }
