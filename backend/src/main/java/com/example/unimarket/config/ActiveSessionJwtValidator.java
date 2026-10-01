package com.example.unimarket.config;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Rejects a signed access token when its account is no longer allowed to
 * authenticate or its server-side refresh-token family has been revoked.
 */
@Component
public class ActiveSessionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error("invalid_token");
    private final IUserAccountRepository accountRepository;
    private final IAuthSessionRepository sessionRepository;

    public ActiveSessionJwtValidator(IUserAccountRepository accountRepository,
                                     IAuthSessionRepository sessionRepository) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        UUID userId = uuidOrNull(token == null ? null : token.getSubject());
        UUID tokenFamilyId = uuidOrNull(token == null ? null : token.getClaimAsString("sid"));
        if (userId == null || tokenFamilyId == null) return invalid();

        UserAccount account = accountRepository.read(userId);
        if (account == null || !account.canAuthenticate(Instant.now())) return invalid();
        return sessionRepository.hasActiveSessionFamily(userId, tokenFamilyId)
                ? OAuth2TokenValidatorResult.success() : invalid();
    }

    private UUID uuidOrNull(String value) {
        try { return value == null ? null : UUID.fromString(value); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
    }
}
