package com.example.unimarket.repository;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.EngagementTrack;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class BadgeAwardRepositoryImpl implements IBadgeAwardRepository {
    private final BadgeAwardJpaRepository jpaRepository;

    public BadgeAwardRepositoryImpl(BadgeAwardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public BadgeAward create(BadgeAward value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public BadgeAward read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public BadgeAward update(BadgeAward value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<BadgeAward> getAll() { return jpaRepository.findAll(); }
    @Override public BadgeAward readByUserIdAndTrackAndBadgeCode(
            UUID userId, EngagementTrack track, BadgeCode badgeCode) {
        return jpaRepository.findByUserIdAndTrackAndBadgeCode(userId, track, badgeCode).orElse(null);
    }
    @Override public List<BadgeAward> readByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByAwardedAtAsc(userId);
    }
    @Override public List<BadgeAward> readByUserIdAndTrack(UUID userId, EngagementTrack track) {
        return jpaRepository.findByUserIdAndTrackOrderByAwardedAtAsc(userId, track);
    }
}
