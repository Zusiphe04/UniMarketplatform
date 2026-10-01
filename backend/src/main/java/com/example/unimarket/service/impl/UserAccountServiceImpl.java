package com.example.unimarket.service.impl;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.UserAccountFactory;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.UpdateAccountStatusRequest;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.service.IRoleService;
import com.example.unimarket.service.IUserAccountService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Administrative account operations.
 */
@Service
public class UserAccountServiceImpl implements IUserAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountServiceImpl.class);

    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;
    private final IAuthSessionRepository sessionRepository;
    private final IRoleService roleService;

    public UserAccountServiceImpl(IUserAccountRepository accountRepository,
                                  IUserProfileRepository profileRepository,
                                  IAuthSessionRepository sessionRepository,
                                  IRoleService roleService) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.sessionRepository = sessionRepository;
        this.roleService = roleService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.getAll().stream()
                .map(account -> AccountResponse.from(
                        account,
                        profileRepository.readByUserId(account.getId()),
                        roleService.getActiveRoles(account.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        UserAccount account = accountRepository.read(accountId);
        if (account == null) {
            throw ResourceNotFoundException.of("Account");
        }
        return AccountResponse.from(
                account,
                profileRepository.readByUserId(accountId),
                roleService.getActiveRoles(accountId));
    }

    @Override
    @Transactional
    public AccountResponse updateStatus(UUID accountId,
                                        UpdateAccountStatusRequest request,
                                        UUID actingAdminId) {

        UserAccount account = accountRepository.read(accountId);
        if (account == null) {
            throw ResourceNotFoundException.of("Account");
        }

        // An administrator locking themselves out would leave the platform
        // without an operator, so the action is refused.
        if (accountId.equals(actingAdminId) && request.status() != AccountStatus.ACTIVE) {
            throw new ValidationException("You cannot suspend or close your own administrator account.");
        }

        UserAccount updated = UserAccountFactory.changeStatus(account, request.status());
        if (updated == null) {
            throw new ValidationException("The status supplied is not valid.");
        }

        UserAccount saved = accountRepository.update(updated);

        // Losing access must be immediate, not deferred until the access token expires.
        if (request.status() != AccountStatus.ACTIVE) {
            sessionRepository.revokeAllForUser(accountId, RevocationReason.ADMIN_ACTION);
        }

        LOGGER.info("Account {} status changed to {} by admin {}. Reason: {}",
                accountId, request.status(), actingAdminId, request.reason());

        return AccountResponse.from(
                saved,
                profileRepository.readByUserId(accountId),
                roleService.getActiveRoles(accountId));
    }

    @Override
    @Transactional
    public boolean deleteAccount(UUID accountId, UUID actingAdminId) {
        if (accountId.equals(actingAdminId)) {
            throw new ValidationException("You cannot close your own administrator account.");
        }

        UserAccount account = accountRepository.read(accountId);
        if (account == null) {
            return false;
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            return true;
        }

        UserAccount closed = UserAccountFactory.changeStatus(account, AccountStatus.CLOSED);
        if (closed == null) {
            throw new ValidationException("This account cannot be closed from its current status.");
        }
        sessionRepository.revokeAllForUser(accountId, RevocationReason.ADMIN_ACTION);
        accountRepository.update(closed);

        LOGGER.warn("Account {} closed by admin {}; historical data was retained.", accountId, actingAdminId);
        return true;
    }
}
