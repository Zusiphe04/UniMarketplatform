package com.example.unimarket.response;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.EngagementTrack;

import java.time.Instant;

public record BadgeResponse(
        BadgeCode code,
        EngagementTrack track,
        String displayName,
        String description,
        Instant awardedAt
) {
    public static BadgeResponse from(BadgeAward award) {
        BadgeCode code = award.getBadgeCode();
        return new BadgeResponse(code, award.getTrack(), code.getDisplayName(),
                code.getDescription(), award.getAwardedAt());
    }
}
