package com.example.unimarket;

import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserRoleAssignmentRepository;
import com.example.unimarket.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RoleServiceImplTest {
    @Test
    void revokingAnActiveRoleRevokesAllAccountSessions() {
        IUserRoleAssignmentRepository roles = mock(IUserRoleAssignmentRepository.class);
        IUserAccountRepository accounts = mock(IUserAccountRepository.class);
        IAuthSessionRepository sessions = mock(IAuthSessionRepository.class);
        RoleServiceImpl service = new RoleServiceImpl(roles, accounts, sessions);
        UUID userId = UUID.randomUUID();
        UserRoleAssignment active = new UserRoleAssignment.Builder().setId(UUID.randomUUID())
                .setUserId(userId).setRole(Role.SELLER).setGrantedAt(Instant.now()).build();
        when(accounts.read(userId)).thenReturn(mock(com.example.unimarket.domain.UserAccount.class));
        when(roles.readActiveByUserIdAndRole(userId, Role.SELLER)).thenReturn(active);
        when(roles.update(active)).thenReturn(active);

        assertTrue(service.revoke(userId, Role.SELLER, UUID.randomUUID()));
        verify(sessions).revokeAllForUser(userId, RevocationReason.ROLE_REVOKED);
    }

    @Test
    void noActiveRoleLeavesSessionsUntouched() {
        IUserRoleAssignmentRepository roles = mock(IUserRoleAssignmentRepository.class);
        IUserAccountRepository accounts = mock(IUserAccountRepository.class);
        IAuthSessionRepository sessions = mock(IAuthSessionRepository.class);
        RoleServiceImpl service = new RoleServiceImpl(roles, accounts, sessions);
        UUID userId = UUID.randomUUID();
        when(accounts.read(userId)).thenReturn(mock(com.example.unimarket.domain.UserAccount.class));
        when(roles.readActiveByUserIdAndRole(userId, Role.SELLER)).thenReturn(null);

        assertFalse(service.revoke(userId, Role.SELLER, UUID.randomUUID()));
        verifyNoInteractions(sessions);
    }
}
