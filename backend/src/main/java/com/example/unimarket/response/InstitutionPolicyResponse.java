package com.example.unimarket.response;

import com.example.unimarket.domain.enums.InstitutionEmailEnforcementMode;

/** Public, frontend-safe institution and mailbox-format policy. */
public record InstitutionPolicyResponse(
        String code,
        String name,
        String institutionType,
        boolean active,
        String unavailableReason,
        EmailPolicy student,
        EmailPolicy faculty
) {
    public record EmailPolicy(
            String domain,
            String formatExample,
            String emailPattern,
            InstitutionEmailEnforcementMode enforcementMode,
            String sourceUrl,
            String sourceCheckedOn
    ) { }
}
