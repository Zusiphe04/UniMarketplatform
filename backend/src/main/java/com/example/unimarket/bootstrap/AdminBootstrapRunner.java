package com.example.unimarket.bootstrap;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.factory.UserAccountFactory;
import com.example.unimarket.factory.UserProfileFactory;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.service.IRoleService;
import com.example.unimarket.util.Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit, idempotent first-admin bootstrap; disabled by default. */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;
    private final IRoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public AdminBootstrapRunner(
            IUserAccountRepository accountRepository,
            IUserProfileRepository profileRepository,
            IRoleService roleService,
            PasswordEncoder passwordEncoder,
            @Value("${unimarket.bootstrap.admin-enabled:false}") boolean enabled,
            @Value("${unimarket.bootstrap.admin-email:}") String email,
            @Value("${unimarket.bootstrap.admin-password:}") String password,
            @Value("${unimarket.bootstrap.admin-first-name:UniMarket}") String firstName,
            @Value("${unimarket.bootstrap.admin-last-name:Administrator}") String lastName) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (!enabled) return;
        if (!Helper.isValidEmail(email) || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin bootstrap requires a valid UNIMARKET_ADMIN_EMAIL and a password of at least 12 characters.");
        }

        UserAccount account = accountRepository.readByEmail(email);
        if (account == null) {
            account = UserAccountFactory.createAccount(email, passwordEncoder.encode(password),
                    "2026-01", "2026-01");
            if (account == null) throw new IllegalStateException("Could not create bootstrap administrator.");
            account = accountRepository.create(account);
        } else if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("The bootstrap administrator account exists but is not active.");
        }

        UserProfile profile = profileRepository.readByUserId(account.getId());
        if (profile == null) {
            profile = UserProfileFactory.createProfile(account.getId(), firstName, lastName, "Administrator");
            profileRepository.create(profile);
        }

        roleService.grantDefaultRole(account.getId());
        roleService.grant(account.getId(), Role.ADMIN, account.getId());
        roleService.grant(account.getId(), Role.MODERATOR, account.getId());
        LOGGER.info("Bootstrap administrator is ready for account {}.", account.getId());
    }
}
