package com.example.unimarket.response;

import com.example.unimarket.domain.UserProfile;

import java.util.List;
import java.util.UUID;

public record EngagementProfileResponse(
        UUID userId,
        String displayName,
        String avatarStorageKey,
        long buyerPoints,
        long sellerPoints,
        List<BadgeResponse> badges
) {
    public static EngagementProfileResponse of(UUID userId, UserProfile profile,
                                               long buyerPoints, long sellerPoints,
                                               List<BadgeResponse> badges) {
        return new EngagementProfileResponse(userId,
                profile == null ? "Community member" : profile.getDisplayName(),
                profile == null ? null : profile.getAvatarStorageKey(),
                buyerPoints, sellerPoints, List.copyOf(badges));
    }
}
