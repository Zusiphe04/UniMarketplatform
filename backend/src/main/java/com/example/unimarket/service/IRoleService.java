package com.example.unimarket.service;

import com.example.unimarket.domain.enums.Role;

import java.util.Set;
import java.util.UUID;

/**
 * Grants and revokes authorization roles.
 */
public interface IRoleService {

    /**
     * Grants the default {@link Role#BUYER} role held by every active member.
     */
    void grantDefaultRole(UUID userId);

    /**
     * Grants a role.
     *
     * @param grantedByUserId the acting administrator; required for privileged
     *                        roles so a grant is always attributable
     * @return {@code true} when the grant was applied, {@code false} when the
     *         account already held the role or the request was not permitted
     */
    boolean grant(UUID userId, Role role, UUID grantedByUserId);

    /**
     * Revokes a role, preserving the historical grant record.
     *
     * @return {@code true} when an active grant was revoked
     */
    boolean revoke(UUID userId, Role role, UUID revokedByUserId);

    /**
     * Returns the roles an account currently holds.
     */
    Set<Role> getActiveRoles(UUID userId);
}
