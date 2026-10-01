package com.example.unimarket.response;

import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;

import java.time.Instant;
import java.util.UUID;

public record LoyaltyPointResponse(
        UUID id,
        EngagementTrack track,
        LoyaltyEventType eventType,
        int points,
        String sourceType,
        UUID sourceId,
        Instant occurredAt
) {
    public static LoyaltyPointResponse from(LoyaltyPointEntry entry) {
        return new LoyaltyPointResponse(entry.getId(), entry.getTrack(), entry.getEventType(),
                entry.getPoints(), entry.getSourceType(), entry.getSourceId(), entry.getOccurredAt());
    }
}
