package com.example.unimarket.repository;

import java.util.List;
import java.util.UUID;

import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.Role;

/**
 * Abstraction over {@link UserRoleAssignment} persistence.
 */
public interface IUserRoleAssignmentRepository extends IRepository<UserRoleAssignment, UUID> {

    /**
     * Returns the account's currently held roles, excluding revoked grants.
     */
    List<UserRoleAssignment> readActiveByUserId(UUID userId);

    /**
     * Finds an active grant of a specific role.
     *
     * @return the assignment, or {@code null} when the account does not hold it
     */
    UserRoleAssignment readActiveByUserIdAndRole(UUID userId, Role role);

    /** Locks all active grants for a role so cross-account invariants can be checked atomically. */
    List<UserRoleAssignment> readActiveByRoleForUpdate(Role role);

    /** Number of active grants for a role, used to preserve a final administrator. */
    long countActiveByRole(Role role);
}
