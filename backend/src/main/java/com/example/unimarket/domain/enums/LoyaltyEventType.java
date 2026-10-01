package com.example.unimarket.domain.enums;

public enum LoyaltyEventType {
    PURCHASE_COMPLETED(EngagementTrack.BUYER, 10),
    SALE_COMPLETED(EngagementTrack.SELLER, 10),
    VERIFIED_REVIEW_CREATED(EngagementTrack.BUYER, 3),
    PRODUCT_PUBLISHED(EngagementTrack.SELLER, 2),
    BULLETIN_PUBLISHED(EngagementTrack.BUYER, 1);

    private final EngagementTrack track;
    private final int points;

    LoyaltyEventType(EngagementTrack track, int points) {
        this.track = track;
        this.points = points;
    }

    public EngagementTrack getTrack() { return track; }
    public int getPoints() { return points; }
}
