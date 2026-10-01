package com.example.unimarket.repository;

import com.example.unimarket.domain.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA access to the {@code user_account} table.
 *
 * <p>This interface is an internal persistence detail. Services depend on
 * {@link IUserAccountRepository} instead, so the storage technology can change
 * without touching the service layer.
 */
@Repository
public interface UserAccountJpaRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
