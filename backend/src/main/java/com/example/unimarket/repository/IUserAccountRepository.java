package com.example.unimarket.repository;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.repository.IRepository;

import java.util.UUID;

/**
 * Abstraction over {@link UserAccount} persistence.
 *
 * <p>Inherits {@code create}, {@code read}, {@code update}, {@code delete},
 * and {@code getAll} from {@link IRepository}, and adds the lookups the
 * authentication flows require.
 */
public interface IUserAccountRepository extends IRepository<UserAccount, UUID> {

    /**
     * Finds an account by email address. The supplied value is normalised
     * before lookup, so casing and surrounding whitespace do not matter.
     *
     * @return the account, or {@code null} when no account uses that address
     */
    UserAccount readByEmail(String email);

    /**
     * Reports whether an account already uses the given address.
     */
    boolean existsByEmail(String email);
}
