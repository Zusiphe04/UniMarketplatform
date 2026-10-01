package com.example.unimarket.repository;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.EngagementTrack;

import java.util.List;
import java.util.UUID;

public interface IBadgeAwardRepository extends IRepository<BadgeAward, UUID> {
    BadgeAward readByUserIdAndTrackAndBadgeCode(UUID userId, EngagementTrack track, BadgeCode badgeCode);
    List<BadgeAward> readByUserId(UUID userId);
    List<BadgeAward> readByUserIdAndTrack(UUID userId, EngagementTrack track);
}
