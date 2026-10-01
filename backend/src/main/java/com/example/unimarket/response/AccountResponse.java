package com.example.unimarket.response;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.Role;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Safe public view of an account.
 *
 * <p>Exposes only what a client legitimately needs. The password hash, token
 * hashes, lockout counters, and acceptance timestamps are deliberately absent,
 * which is the reason entities are never returned directly from a controller.
 */
public record AccountResponse(
        UUID id,
        String email,
        String displayName,
        String fullName,
        AccountStatus status,
        boolean emailVerified,
        List<Role> roles,
        List<PersonaResponse> personas
) {

    /**
     * Maps an account, its profile, and its active roles into a response.
     *
     * @param profile may be {@code null} if the profile has not been created
     */
    public static AccountResponse from(UserAccount account,
                                       UserProfile profile,
                                       Set<Role> roles,
                                       List<PersonaResponse> personas) {
        if (account == null) {
            return null;
        }
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                (profile != null) ? profile.getDisplayName() : null,
                (profile != null) ? profile.getFullName() : null,
                account.getStatus(),
                account.isEmailVerified(),
                (roles != null) ? List.copyOf(roles) : Collections.emptyList(),
                (personas != null) ? List.copyOf(personas) : Collections.emptyList()
        );
    }

    /** Backward-compatible mapper for flows that do not load persona assignments. */
    public static AccountResponse from(UserAccount account, UserProfile profile, Set<Role> roles) {
        return from(account, profile, roles, Collections.emptyList());
    }
}
