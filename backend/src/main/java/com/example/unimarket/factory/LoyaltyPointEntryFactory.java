package com.example.unimarket.factory;

import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.enums.LoyaltyEventType;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class LoyaltyPointEntryFactory {
    private LoyaltyPointEntryFactory() { }

    public static LoyaltyPointEntry create(UUID userId, LoyaltyEventType eventType,
                                           String sourceType, UUID sourceId,
                                           String eventKey, Instant occurredAt) {
        String cleanSourceType = Helper.cleanText(sourceType);
        String cleanEventKey = eventKey == null ? null : eventKey.trim();
        if (userId == null || eventType == null || sourceId == null || occurredAt == null
                || cleanSourceType == null || cleanSourceType.length() > 40
                || cleanEventKey == null || cleanEventKey.isBlank() || cleanEventKey.length() > 180) return null;
        return new LoyaltyPointEntry.Builder().setId(Helper.generateId()).setUserId(userId)
                .setTrack(eventType.getTrack()).setEventType(eventType)
                .setSourceType(cleanSourceType).setSourceId(sourceId).setEventKey(cleanEventKey)
                .setPoints(eventType.getPoints()).setOccurredAt(occurredAt).build();
    }
}
