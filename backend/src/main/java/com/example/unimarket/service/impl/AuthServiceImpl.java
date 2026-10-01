package com.example.unimarket.service.impl;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.exception.InvalidCredentialsException;
import com.example.unimarket.exception.InvalidTokenException;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.AuthSessionFactory;
import com.example.unimarket.factory.UserAccountFactory;
import com.example.unimarket.factory.UserProfileFactory;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserPersonaAssignmentRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.ChangePasswordRequest;
import com.example.unimarket.request.LoginRequest;
import com.example.unimarket.request.RegisterRequest;
import com.example.unimarket.response.AccessTokenResponse;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.response.AuthResponse;
import com.example.unimarket.response.PersonaResponse;
import com.example.unimarket.response.RegistrationResponse;
import com.example.unimarket.service.IAuthService;
import com.example.unimarket.service.IRoleService;
import com.example.unimarket.service.ITokenService;
import com.example.unimarket.util.Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Ecommerce-style registration, login, refresh, logout, and password management. */
@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String CURRENT_TERMS_VERSION = "2026-01";
    private static final String CURRENT_PRIVACY_VERSION = "2026-01";
    private static final String DUMMY_HASH =
            "$2a$12$C6UzMDM.H6dfI/f/IKcEe.WlLdRUEnJXcMSFqzKQFUwPmcJ7CuvJi";

    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;
    private final IUserPersonaAssignmentRepository personaRepository;
    private final IAuthSessionRepository sessionRepository;
    private final IRoleService roleService;
    private final ITokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final Duration refreshTokenTtl;
    private final int maxFailedLoginAttempts;
    private final Duration lockoutDuration;

    public AuthServiceImpl(IUserAccountRepository accountRepository,
                           IUserProfileRepository profileRepository,
                           IUserPersonaAssignmentRepository personaRepository,
                           IAuthSessionRepository sessionRepository,
                           IRoleService roleService,
                           ITokenService tokenService,
                           PasswordEncoder passwordEncoder,
                           @Value("${unimarket.security.refresh-token-ttl}") Duration refreshTokenTtl,
                           @Value("${unimarket.security.max-failed-login-attempts}") int maxFailedLoginAttempts,
                           @Value("${unimarket.security.lockout-duration}") Duration lockoutDuration) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
        this.sessionRepository = sessionRepository;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenTtl = refreshTokenTtl;
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        this.lockoutDuration = lockoutDuration;
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (!request.acceptedTerms() || !request.acceptedPrivacy()) {
            throw new ValidationException("The terms of use and privacy notice must both be accepted.");
        }

        String normalizedEmail = Helper.normalizeEmail(request.email());
        if (accountRepository.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with this email address already exists.");
        }

        UserAccount account = UserAccountFactory.createAccount(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                CURRENT_TERMS_VERSION,
                CURRENT_PRIVACY_VERSION);
        if (account == null) {
            throw new ValidationException("The registration details supplied are not valid.");
        }

        UserAccount savedAccount = accountRepository.create(account);
        UserProfile profile = UserProfileFactory.createProfile(
                savedAccount.getId(), request.firstName(), request.lastName(), request.displayName());
        if (profile == null) {
            throw new ValidationException("The name details supplied are not valid.");
        }

        profileRepository.create(profile);
        roleService.grantDefaultRole(savedAccount.getId());
        return RegistrationResponse.registered(savedAccount.getId());
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginOutcome login(LoginRequest request, SessionContext sessionContext) {
        UserAccount account = accountRepository.readByEmail(request.email());
        if (account == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            accountRepository.update(UserAccountFactory.recordFailedLogin(
                    account, maxFailedLoginAttempts, lockoutDuration));
            throw new InvalidCredentialsException();
        }

        if (!account.canAuthenticate(Instant.now())) {
            throw new InvalidCredentialsException();
        }

        UserAccount loggedIn = accountRepository.update(UserAccountFactory.recordSuccessfulLogin(account));
        return startSession(loggedIn, sessionContext);
    }

    @Override
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public LoginOutcome refresh(String rawRefreshToken, SessionContext sessionContext) {
        if (Helper.isNullOrEmpty(rawRefreshToken)) {
            throw InvalidTokenException.refreshToken();
        }

        AuthSession current = sessionRepository.readByRawTokenForUpdate(rawRefreshToken);
        if (current == null) {
            throw InvalidTokenException.refreshToken();
        }
        if (current.isRevoked()) {
            sessionRepository.revokeFamily(current.getTokenFamilyId(), RevocationReason.REUSE_DETECTED);
            LOGGER.warn("Refresh token reuse detected. Revoked token family {}.", current.getTokenFamilyId());
            throw InvalidTokenException.refreshToken();
        }
        if (current.isExpired(Instant.now())) {
            throw InvalidTokenException.refreshToken();
        }

        UserAccount account = accountRepository.read(current.getUserId());
        if (account == null || !account.canAuthenticate(Instant.now())) {
            throw InvalidTokenException.refreshToken();
        }
        if (current.predatesCredentialChange(account.getCredentialsChangedAt())) {
            sessionRepository.revokeFamily(current.getTokenFamilyId(), RevocationReason.PASSWORD_CHANGE);
            throw InvalidTokenException.refreshToken();
        }

        AuthSessionFactory.IssuedSession replacement = AuthSessionFactory.rotateSession(
                current, refreshTokenTtl, sessionContext.ipAddress(), sessionContext.userAgent());
        AuthSession savedReplacement = sessionRepository.create(replacement.session());
        sessionRepository.update(AuthSessionFactory.markRotated(current, savedReplacement.getId()));
        return buildOutcome(account, savedReplacement, replacement.rawRefreshToken());
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (Helper.isNullOrEmpty(rawRefreshToken)) return;
        AuthSession session = sessionRepository.readByRawTokenForUpdate(rawRefreshToken);
        if (session != null) {
            // Logout invalidates the whole rotation family. If refresh won a race and already
            // rotated this token, its replacement is revoked in the same family as well.
            sessionRepository.revokeFamily(session.getTokenFamilyId(), RevocationReason.LOGOUT);
        }
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        sessionRepository.revokeAllForUser(userId, RevocationReason.LOGOUT_ALL);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        UserAccount account = accountRepository.read(userId);
        if (account == null) throw ResourceNotFoundException.of("Account");
        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        UserAccount updated = UserAccountFactory.changePassword(
                account, passwordEncoder.encode(request.newPassword()));
        if (updated == null) throw new ValidationException("The new password is not valid.");
        accountRepository.update(updated);
        sessionRepository.revokeAllForUser(account.getId(), RevocationReason.PASSWORD_CHANGE);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getCurrentAccount(UUID userId) {
        UserAccount account = accountRepository.read(userId);
        if (account == null) throw ResourceNotFoundException.of("Account");
        return buildAccountResponse(account);
    }

    private LoginOutcome startSession(UserAccount account, SessionContext context) {
        AuthSessionFactory.IssuedSession issued = AuthSessionFactory.createSession(
                account.getId(), refreshTokenTtl, context.ipAddress(), context.userAgent());
        AuthSession session = sessionRepository.create(issued.session());
        return buildOutcome(account, session, issued.rawRefreshToken());
    }

    private LoginOutcome buildOutcome(UserAccount account, AuthSession session, String rawRefreshToken) {
        Set<Role> roles = roleService.getActiveRoles(account.getId());
        AccessTokenResponse accessToken = tokenService.issueAccessToken(
                account, roles, session.getTokenFamilyId().toString());
        UserProfile profile = profileRepository.readByUserId(account.getId());
        AuthResponse response = AuthResponse.of(accessToken.token(), accessToken.expiresInSeconds(),
                AccountResponse.from(account, profile, roles, personaResponses(account.getId())));
        return new LoginOutcome(response, rawRefreshToken, refreshTokenTtl.getSeconds());
    }

    private AccountResponse buildAccountResponse(UserAccount account) {
        return AccountResponse.from(account,
                profileRepository.readByUserId(account.getId()),
                roleService.getActiveRoles(account.getId()),
                personaResponses(account.getId()));
    }

    private List<PersonaResponse> personaResponses(UUID userId) {
        return personaRepository.readByUserId(userId).stream().map(PersonaResponse::from).toList();
    }
}
