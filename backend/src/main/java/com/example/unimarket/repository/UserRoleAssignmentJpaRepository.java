package com.example.unimarket.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.Role;

import jakarta.persistence.LockModeType;

/**
 * Spring Data JPA access to the {@code user_role_assignment} table.
 */
public interface UserRoleAssignmentJpaRepository extends JpaRepository<UserRoleAssignment, UUID> {

    List<UserRoleAssignment> findByUserIdAndRevokedAtIsNull(UUID userId);

    long countByRoleAndRevokedAtIsNull(Role role);

    Optional<UserRoleAssignment> findByUserIdAndRoleAndRevokedAtIsNull(UUID userId, Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserRoleAssignment> findByRoleAndRevokedAtIsNullOrderByIdAsc(Role role);
}
