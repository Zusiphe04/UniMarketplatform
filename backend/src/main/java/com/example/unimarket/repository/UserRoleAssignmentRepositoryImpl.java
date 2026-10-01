package com.example.unimarket.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.unimarket.domain.UserRoleAssignment;
import com.example.unimarket.domain.enums.Role;

/**
 * Singleton implementation of {@link IUserRoleAssignmentRepository}.
 */
@Repository
public class UserRoleAssignmentRepositoryImpl implements IUserRoleAssignmentRepository {

    private static volatile UserRoleAssignmentRepositoryImpl instance;

    private final UserRoleAssignmentJpaRepository jpaRepository;

    public UserRoleAssignmentRepositoryImpl(UserRoleAssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public static UserRoleAssignmentRepositoryImpl getInstance(UserRoleAssignmentJpaRepository jpaRepository) {
        if (instance == null) {
            synchronized (UserRoleAssignmentRepositoryImpl.class) {
                if (instance == null) {
                    instance = new UserRoleAssignmentRepositoryImpl(jpaRepository);
                }
            }
        }
        return instance;
    }

    @Override
    public UserRoleAssignment create(UserRoleAssignment entity) {
        return entity == null ? null : jpaRepository.save(entity);
    }

    @Override
    public UserRoleAssignment read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }

    @Override
    public UserRoleAssignment update(UserRoleAssignment entity) {
        if (entity == null || entity.getId() == null || !jpaRepository.existsById(entity.getId())) {
            return null;
        }
        return jpaRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public List<UserRoleAssignment> getAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<UserRoleAssignment> readActiveByUserId(UUID userId) {
        return userId == null ? List.of() : jpaRepository.findByUserIdAndRevokedAtIsNull(userId);
    }

    @Override
    public UserRoleAssignment readActiveByUserIdAndRole(UUID userId, Role role) {
        if (userId == null || role == null) {
            return null;
        }
        return jpaRepository.findByUserIdAndRoleAndRevokedAtIsNull(userId, role).orElse(null);
    }

    @Override
    public List<UserRoleAssignment> readActiveByRoleForUpdate(Role role) {
        return role == null ? List.of() : jpaRepository.findByRoleAndRevokedAtIsNullOrderByIdAsc(role);
    }

    @Override
    public long countActiveByRole(Role role) {
        return role == null ? 0 : jpaRepository.countByRoleAndRevokedAtIsNull(role);
    }
}
