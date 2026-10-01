package com.example.unimarket.factory;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class BadgeAwardFactory {
    private BadgeAwardFactory() { }

    public static BadgeAward create(UUID userId, BadgeCode badgeCode, Instant awardedAt) {
        if (userId == null || badgeCode == null || awardedAt == null) return null;
        return new BadgeAward.Builder().setId(Helper.generateId()).setUserId(userId)
                .setTrack(badgeCode.getTrack()).setBadgeCode(badgeCode).setAwardedAt(awardedAt).build();
    }
}
