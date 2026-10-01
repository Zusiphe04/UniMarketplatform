package com.example.unimarket.request;

import com.example.unimarket.domain.enums.Role;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for an administrator granting a role.
 *
 * <p>The granting actor is taken from the authenticated principal rather than
 * from this payload, so a caller cannot attribute a grant to someone else.
 */
public record GrantRoleRequest(

        @NotNull(message = "Role is required")
        Role role
) {
}
