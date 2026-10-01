package com.example.unimarket.repository;

import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;
import com.example.unimarket.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LoyaltyPointRepositoryImpl implements ILoyaltyPointRepository {
    private final LoyaltyPointEntryJpaRepository jpaRepository;

    public LoyaltyPointRepositoryImpl(LoyaltyPointEntryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public LoyaltyPointEntry create(LoyaltyPointEntry value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public LoyaltyPointEntry read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public LoyaltyPointEntry update(LoyaltyPointEntry value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<LoyaltyPointEntry> getAll() { return jpaRepository.findAll(); }
    @Override public LoyaltyPointEntry readByUserIdAndTrackAndEventKey(
            UUID userId, EngagementTrack track, String eventKey) {
        return jpaRepository.findByUserIdAndTrackAndEventKey(userId, track, eventKey).orElse(null);
    }
    @Override public Page<LoyaltyPointEntry> readByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable);
    }
    @Override public long totalPoints(UUID userId, EngagementTrack track) {
        Long total = jpaRepository.totalPoints(userId, track);
        return total == null ? 0 : total;
    }
    @Override public long countEvents(UUID userId, EngagementTrack track, LoyaltyEventType eventType) {
        return jpaRepository.countByUserIdAndTrackAndEventType(userId, track, eventType);
    }
    @Override public Page<LoyaltyLeaderboardProjection> readLeaderboard(
            EngagementTrack track, Role role, AccountStatus accountStatus, Pageable pageable) {
        return jpaRepository.leaderboard(track, role, accountStatus, pageable);
    }
}
