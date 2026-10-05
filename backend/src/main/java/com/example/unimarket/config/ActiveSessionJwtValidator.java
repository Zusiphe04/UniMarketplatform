package com.example.unimarket.config;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserRoleAssignmentRepository;

/** Rejects access tokens whose account, session family, or embedded roles are no longer current. */
@Component
public class ActiveSessionJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error("invalid_token");
    private final IUserAccountRepository accountRepository;
    private final IAuthSessionRepository sessionRepository;
    private final IUserRoleAssignmentRepository roleRepository;

    public ActiveSessionJwtValidator(IUserAccountRepository accountRepository,
                                     IAuthSessionRepository sessionRepository,
                                     IUserRoleAssignmentRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        UUID userId = uuidOrNull(token == null ? null : token.getSubject());
        UUID tokenFamilyId = uuidOrNull(token == null ? null : token.getClaimAsString("sid"));
        if (userId == null || tokenFamilyId == null) return invalid();

        UserAccount account = accountRepository.read(userId);
        if (account == null || !account.canAuthenticate(Instant.now())) return invalid();
        if (!sessionRepository.hasActiveSessionFamily(userId, tokenFamilyId)) return invalid();

        List<String> claimRoles;
        try {
            claimRoles = token.getClaimAsStringList("roles");
        } catch (RuntimeException exception) {
            return invalid();
        }
        if (claimRoles == null || claimRoles.stream().anyMatch(value -> value == null || value.isBlank())) return invalid();
        Set<String> persistedRoles = roleRepository.readActiveByUserId(userId).stream()
                .map(assignment -> assignment.getRole().name()).collect(Collectors.toSet());
        return persistedRoles.equals(new HashSet<>(claimRoles))
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
