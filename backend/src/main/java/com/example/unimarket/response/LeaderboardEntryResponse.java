package com.example.unimarket.response;

import com.example.unimarket.domain.enums.EngagementTrack;

import java.util.List;
import java.util.UUID;

public record LeaderboardEntryResponse(
        long rank,
        UUID userId,
        String displayName,
        String avatarStorageKey,
        EngagementTrack track,
        long points,
        List<BadgeResponse> badges
) { }
