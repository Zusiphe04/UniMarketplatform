package com.example.unimarket.service.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.UserRoleAssignmentFactory;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserRoleAssignmentRepository;
import com.example.unimarket.service.IRoleService;

/** Grants and revokes roles through the repository abstraction. */
@Service
public class RoleServiceImpl implements IRoleService {
    private final IUserRoleAssignmentRepository roleRepository;
    private final IUserAccountRepository accountRepository;
    private final IAuthSessionRepository sessionRepository;

    public RoleServiceImpl(IUserRoleAssignmentRepository roleRepository,
                           IUserAccountRepository accountRepository,
                           IAuthSessionRepository sessionRepository) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public void grantDefaultRole(UUID userId) {
        if (roleRepository.readActiveByUserIdAndRole(userId, Role.BUYER) != null) return;
        UserRoleAssignment assignment = UserRoleAssignmentFactory.createBuyerRole(userId);
        if (assignment != null) roleRepository.create(assignment);
    }

    @Override
    @Transactional
    public boolean grant(UUID userId, Role role, UUID grantedByUserId) {
        if (userId == null || role == null) return false;
        var account = accountRepository.read(userId);
        if (account == null) throw ResourceNotFoundException.of("Account");
        if (!account.getStatus().canAuthenticate() || !account.isEmailVerified()) {
            throw new ValidationException("Roles can only be granted to an active, verified account.");
        }
        if (roleRepository.readActiveByUserIdAndRole(userId, role) != null) return false;
        UserRoleAssignment assignment = UserRoleAssignmentFactory.createAssignment(userId, role, grantedByUserId);
        if (assignment == null) return false;
        roleRepository.create(assignment);
        return true;
    }

    @Override
    @Transactional
    public boolean revoke(UUID userId, Role role, UUID revokedByUserId) {
        if (userId == null || role == null || accountRepository.read(userId) == null) {
            throw ResourceNotFoundException.of("Account");
        }
        if (role == Role.BUYER) {
            throw new ValidationException("The base BUYER role cannot be revoked from an active account.");
        }
        if (role == Role.ADMIN && userId.equals(revokedByUserId)) {
            throw new ValidationException("You cannot revoke your own ADMIN role.");
        }
        UserRoleAssignment active;
        if (role == Role.ADMIN) {
            // Lock the complete active ADMIN set. Concurrent cross-revocations now serialize,
            // so the second transaction observes the first revocation before checking size.
            List<UserRoleAssignment> activeAdmins = roleRepository.readActiveByRoleForUpdate(Role.ADMIN);
            active = activeAdmins.stream().filter(assignment -> assignment.getUserId().equals(userId)).findFirst().orElse(null);
            if (active == null) return false;
            if (activeAdmins.size() <= 1) {
                throw new ValidationException("You cannot revoke the final active ADMIN role.");
            }
        } else {
            active = roleRepository.readActiveByUserIdAndRole(userId, role);
        }
        if (active == null) return false;
        UserRoleAssignment revoked = UserRoleAssignmentFactory.revoke(active, revokedByUserId);
        boolean updated = revoked != null && roleRepository.update(revoked) != null;
        if (updated) sessionRepository.revokeAllForUser(userId, RevocationReason.ROLE_REVOKED);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> getActiveRoles(UUID userId) {
        List<UserRoleAssignment> assignments = roleRepository.readActiveByUserId(userId);
        Set<Role> roles = EnumSet.noneOf(Role.class);
        assignments.forEach(assignment -> roles.add(assignment.getRole()));
        return roles;
    }
}
