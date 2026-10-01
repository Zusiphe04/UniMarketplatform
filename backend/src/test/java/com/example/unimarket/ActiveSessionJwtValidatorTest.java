package com.example.unimarket;

import com.example.unimarket.config.ActiveSessionJwtValidator;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActiveSessionJwtValidatorTest {
    @Test
    void acceptsOnlyAnAuthenticatableAccountWithAnActiveMatchingSessionFamily() {
        IUserAccountRepository accounts = mock(IUserAccountRepository.class);
        IAuthSessionRepository sessions = mock(IAuthSessionRepository.class);
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(accounts, sessions);
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        when(accounts.read(userId)).thenReturn(activeAccount(userId));
        when(sessions.hasActiveSessionFamily(userId, familyId)).thenReturn(true);

        assertFalse(validator.validate(token(userId, familyId)).hasErrors());

        when(sessions.hasActiveSessionFamily(userId, familyId)).thenReturn(false);
        assertTrue(validator.validate(token(userId, familyId)).hasErrors());
    }

    @Test
    void rejectsMissingOrMalformedStatefulClaimsBeforeRepositoryAccess() {
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(
                mock(IUserAccountRepository.class), mock(IAuthSessionRepository.class));
        Jwt missingSid = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", UUID.randomUUID().toString()));
        Jwt malformedSubject = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", "not-a-uuid", "sid", UUID.randomUUID().toString()));

        assertTrue(validator.validate(missingSid).hasErrors());
        assertTrue(validator.validate(malformedSubject).hasErrors());
    }

    private UserAccount activeAccount(UUID id) {
        Instant now = Instant.now();
        return new UserAccount.Builder().setId(id).setEmail("buyer@example.test").setPasswordHash("hash")
                .setStatus(AccountStatus.ACTIVE).setEmailVerifiedAt(now).setCredentialsChangedAt(now)
                .setTermsVersion("1").setTermsAcceptedAt(now).setPrivacyVersion("1").setPrivacyAcceptedAt(now).build();
    }

    private Jwt token(UUID userId, UUID familyId) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", userId.toString(), "sid", familyId.toString()));
    }
}
