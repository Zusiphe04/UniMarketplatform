package com.example.unimarket.factory;

import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

/**
 * Creates valid {@link UserRoleAssignment} aggregates.
 *
 * <p>Privileged roles must name the actor who granted them, which is enforced
 * here rather than left to each caller.
 */
public final class UserRoleAssignmentFactory {

    private UserRoleAssignmentFactory() {
        // Factory class.
    }

    /**
     * Grants the default {@link Role#BUYER} role that every active member holds.
     */
    public static UserRoleAssignment createBuyerRole(UUID userId) {
        if (userId == null) {
            return null;
        }
        return new UserRoleAssignment.Builder()
                .setId(Helper.generateId())
                .setUserId(userId)
                .setRole(Role.BUYER)
                .setGrantedAt(Instant.now())
                .setGrantedByUserId(null)
                .build();
    }

    /**
     * Grants a role to an account.
     *
     * @param grantedByUserId the acting administrator, required for privileged roles
     * @return the assignment, or {@code null} when the input is not valid
     */
    public static UserRoleAssignment createAssignment(UUID userId, Role role, UUID grantedByUserId) {
        if (userId == null || role == null) {
            return null;
        }
        if (role.isPrivileged() && grantedByUserId == null) {
            return null;
        }
        return new UserRoleAssignment.Builder()
                .setId(Helper.generateId())
                .setUserId(userId)
                .setRole(role)
                .setGrantedAt(Instant.now())
                .setGrantedByUserId(grantedByUserId)
                .build();
    }

    /**
     * Returns a revoked copy of the assignment, preserving the original grant.
     *
     * @return the revoked assignment, or {@code null} when already revoked
     */
    public static UserRoleAssignment revoke(UserRoleAssignment assignment, UUID revokedByUserId) {
        if (assignment == null || !assignment.revoke(revokedByUserId, Instant.now())) {
            return null;
        }
        return assignment;
    }
}
