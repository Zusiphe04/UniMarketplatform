package com.example.unimarket;

import com.example.unimarket.config.ActiveSessionJwtValidator;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserRoleAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActiveSessionJwtValidatorTest {
    @Test
    void acceptsOnlyAnAuthenticatableAccountWithActiveSessionAndCurrentRoles() {
        IUserAccountRepository accounts = mock(IUserAccountRepository.class);
        IAuthSessionRepository sessions = mock(IAuthSessionRepository.class);
        IUserRoleAssignmentRepository roles = mock(IUserRoleAssignmentRepository.class);
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(accounts, sessions, roles);
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UserRoleAssignment buyerRole = role(Role.BUYER);
        when(accounts.read(userId)).thenReturn(activeAccount(userId));
        when(sessions.hasActiveSessionFamily(userId, familyId)).thenReturn(true);
        when(roles.readActiveByUserId(userId)).thenReturn(List.of(buyerRole));

        assertFalse(validator.validate(token(userId, familyId, List.of("BUYER"))).hasErrors());

        when(roles.readActiveByUserId(userId)).thenReturn(List.of(buyerRole, role(Role.SELLER)));
        assertTrue(validator.validate(token(userId, familyId, List.of("BUYER"))).hasErrors());

        when(sessions.hasActiveSessionFamily(userId, familyId)).thenReturn(false);
        assertTrue(validator.validate(token(userId, familyId, List.of("BUYER", "SELLER"))).hasErrors());
    }

    @Test
    void rejectsMissingOrMalformedStatefulClaimsBeforeRepositoryAccess() {
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(
                mock(IUserAccountRepository.class), mock(IAuthSessionRepository.class),
                mock(IUserRoleAssignmentRepository.class));
        Jwt missingSid = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", UUID.randomUUID().toString()));
        Jwt malformedSubject = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", "not-a-uuid", "sid", UUID.randomUUID().toString()));

        assertTrue(validator.validate(missingSid).hasErrors());
        assertTrue(validator.validate(malformedSubject).hasErrors());
    }

    private UserRoleAssignment role(Role role) {
        return new UserRoleAssignment.Builder().setId(UUID.randomUUID()).setUserId(UUID.randomUUID())
                .setRole(role).setGrantedAt(Instant.now()).build();
    }

    private UserAccount activeAccount(UUID id) {
        Instant now = Instant.now();
        return new UserAccount.Builder().setId(id).setEmail("buyer@example.test").setPasswordHash("hash")
                .setStatus(AccountStatus.ACTIVE).setEmailVerifiedAt(now).setCredentialsChangedAt(now)
                .setTermsVersion("1").setTermsAcceptedAt(now).setPrivacyVersion("1").setPrivacyAcceptedAt(now).build();
    }

    private Jwt token(UUID userId, UUID familyId, List<String> roles) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", userId.toString(), "sid", familyId.toString(), "roles", roles));
    }
}
