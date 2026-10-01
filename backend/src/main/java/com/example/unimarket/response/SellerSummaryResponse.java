package com.example.unimarket.response;

import com.example.unimarket.domain.UserProfile;

import java.util.UUID;

/** Intentionally limited seller information safe for public catalogue responses. */
public record SellerSummaryResponse(UUID id, String displayName) {
    public static SellerSummaryResponse from(UUID userId, UserProfile profile) {
        String name = profile == null || profile.getDisplayName() == null || profile.getDisplayName().isBlank()
                ? "UniMarket seller" : profile.getDisplayName();
        return new SellerSummaryResponse(userId, name);
    }
}
