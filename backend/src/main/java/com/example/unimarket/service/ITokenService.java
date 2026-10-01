package com.example.unimarket.service;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.response.AccessTokenResponse;

import java.util.Set;

/**
 * Mints signed JWT access tokens.
 *
 * <p>Separated from {@link IAuthService} so that token creation can be reused
 * by both login and refresh without duplicating claim construction.
 */
public interface ITokenService {

    /**
     * Issues a short-lived access token for the account.
     *
     * @param roles         the account's active roles, written to the {@code roles} claim
     * @param tokenFamilyId the session family, written to the {@code sid} claim so a
     *                      token can later be correlated with a revoked session
     */
    AccessTokenResponse issueAccessToken(UserAccount account, Set<Role> roles, String tokenFamilyId);
}
