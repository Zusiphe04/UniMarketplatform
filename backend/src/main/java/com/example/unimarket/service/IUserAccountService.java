package com.example.unimarket.service;

import com.example.unimarket.request.UpdateAccountStatusRequest;
import com.example.unimarket.response.AccountResponse;

import java.util.List;
import java.util.UUID;

/**
 * Administrative account operations.
 *
 * <p>Exposes the repository's read, update, delete, and getAll operations
 * through an authorised, audited service rather than letting a controller reach
 * persistence directly.
 */
public interface IUserAccountService {

    /**
     * Lists every account.
     *
     * <p>Unpaginated for now, which is acceptable at project scale but must
     * become paginated before this endpoint faces real data volumes.
     */
    List<AccountResponse> getAllAccounts();

    /**
     * Reads a single account.
     *
     * @throws com.example.unimarket.exception.ResourceNotFoundException when absent
     */
    AccountResponse getAccount(UUID accountId);

    /**
     * Changes an account's status, for example to suspend or reinstate it.
     *
     * <p>Suspending also revokes every active session, so the member loses
     * access immediately rather than when their access token happens to expire.
     */
    AccountResponse updateStatus(UUID accountId, UpdateAccountStatusRequest request, UUID actingAdminId);

    /**
     * Permanently deletes an account.
     *
     * <p>Retained mainly to complete the repository contract. Suspension is the
     * correct action for policy violations, because deletion destroys the
     * history that later orders, reviews, and audit records depend on.
     *
     * @return {@code true} when an account was deleted
     */
    boolean deleteAccount(UUID accountId, UUID actingAdminId);
}
