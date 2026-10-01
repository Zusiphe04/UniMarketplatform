package com.example.unimarket.repository;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.EngagementTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BadgeAwardJpaRepository extends JpaRepository<BadgeAward, UUID> {
    Optional<BadgeAward> findByUserIdAndTrackAndBadgeCode(
            UUID userId, EngagementTrack track, BadgeCode badgeCode);
    List<BadgeAward> findByUserIdOrderByAwardedAtAsc(UUID userId);
    List<BadgeAward> findByUserIdAndTrackOrderByAwardedAtAsc(UUID userId, EngagementTrack track);
}
