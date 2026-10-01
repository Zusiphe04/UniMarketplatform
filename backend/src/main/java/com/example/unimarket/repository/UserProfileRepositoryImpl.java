package com.example.unimarket.repository;

import com.example.unimarket.domain.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class UserProfileRepositoryImpl implements IUserProfileRepository {
    private final UserProfileJpaRepository jpaRepository;
    public UserProfileRepositoryImpl(UserProfileJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    @Override public UserProfile create(UserProfile entity) { return entity == null ? null : jpaRepository.save(entity); }
    @Override public UserProfile read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public UserProfile update(UserProfile entity) {
        return entity == null || entity.getId() == null || !jpaRepository.existsById(entity.getId()) ? null : jpaRepository.save(entity);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<UserProfile> getAll() { return jpaRepository.findAll(); }
    @Override public UserProfile readByUserId(UUID userId) {
        return userId == null ? null : jpaRepository.findByUserId(userId).orElse(null);
    }
    @Override public List<UserProfile> readByUserIds(Collection<UUID> userIds) {
        return userIds == null || userIds.isEmpty() ? List.of() : jpaRepository.findByUserIdIn(userIds);
    }
}
