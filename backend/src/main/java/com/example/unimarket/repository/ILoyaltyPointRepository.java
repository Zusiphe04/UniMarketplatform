package com.example.unimarket.repository;

import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;
import com.example.unimarket.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ILoyaltyPointRepository extends IRepository<LoyaltyPointEntry, UUID> {
    LoyaltyPointEntry readByUserIdAndTrackAndEventKey(UUID userId, EngagementTrack track, String eventKey);
    Page<LoyaltyPointEntry> readByUserId(UUID userId, Pageable pageable);
    long totalPoints(UUID userId, EngagementTrack track);
    long countEvents(UUID userId, EngagementTrack track, LoyaltyEventType eventType);
    Page<LoyaltyLeaderboardProjection> readLeaderboard(EngagementTrack track, Role role,
                                                        AccountStatus accountStatus, Pageable pageable);
}
