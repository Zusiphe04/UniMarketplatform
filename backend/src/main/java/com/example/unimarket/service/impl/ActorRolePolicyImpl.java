package com.example.unimarket.service.impl;

import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.IRoleService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ActorRolePolicyImpl implements IActorRolePolicy {
    private final IRoleService roleService;

    public ActorRolePolicyImpl(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void requireBuyerOnly(UUID actorId) {
        Set<Role> roles = currentRoles(actorId);
        if (!roles.contains(Role.BUYER) || roles.contains(Role.SELLER) || roles.contains(Role.ADMIN)) {
            throw new AccessDeniedException("Vendor and administrator accounts cannot use buyer or community features.");
        }
    }

    @Override
    public void requireSellerOnly(UUID actorId) {
        Set<Role> roles = currentRoles(actorId);
        if (!roles.contains(Role.SELLER) || roles.contains(Role.MODERATOR) || roles.contains(Role.ADMIN)) {
            throw new AccessDeniedException("Only vendor accounts can manage marketplace listings and seller orders.");
        }
    }

    private Set<Role> currentRoles(UUID actorId) {
        if (actorId == null) throw new AccessDeniedException("Authentication required.");
        return roleService.getActiveRoles(actorId);
    }
}
