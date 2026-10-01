package com.example.unimarket.repository;

import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.CommunityPersona;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Manual Singleton adapter required by the repository rubric. */
@Repository
public class UserPersonaAssignmentRepositoryImpl implements IUserPersonaAssignmentRepository {
    private static volatile UserPersonaAssignmentRepositoryImpl instance;
    private final UserPersonaAssignmentJpaRepository jpaRepository;

    public UserPersonaAssignmentRepositoryImpl(UserPersonaAssignmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public static UserPersonaAssignmentRepositoryImpl getInstance(UserPersonaAssignmentJpaRepository repository) {
        if (instance == null) {
            synchronized (UserPersonaAssignmentRepositoryImpl.class) {
                if (instance == null) instance = new UserPersonaAssignmentRepositoryImpl(repository);
            }
        }
        return instance;
    }

    @Override public UserPersonaAssignment create(UserPersonaAssignment entity) {
        return entity == null ? null : jpaRepository.save(entity);
    }
    @Override public UserPersonaAssignment read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public UserPersonaAssignment update(UserPersonaAssignment entity) {
        return entity == null || entity.getId() == null || !jpaRepository.existsById(entity.getId())
                ? null : jpaRepository.save(entity);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<UserPersonaAssignment> getAll() { return jpaRepository.findAll(); }
    @Override public List<UserPersonaAssignment> readByUserId(UUID userId) {
        return userId == null ? List.of() : jpaRepository.findByUserIdOrderByPersona(userId);
    }
    @Override public UserPersonaAssignment readByUserIdAndPersona(UUID userId, CommunityPersona persona) {
        return userId == null || persona == null ? null
                : jpaRepository.findByUserIdAndPersona(userId, persona).orElse(null);
    }
}
