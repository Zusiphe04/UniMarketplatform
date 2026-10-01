package com.example.unimarket.domain.enums;

public enum BadgeCode {
    FIRST_PURCHASE(EngagementTrack.BUYER, "First Purchase", "Completed a first marketplace purchase."),
    LOYAL_BUYER(EngagementTrack.BUYER, "Loyal Buyer", "Completed five marketplace purchases."),
    VERIFIED_REVIEWER(EngagementTrack.BUYER, "Verified Reviewer", "Shared feedback after a verified purchase."),
    COMMUNITY_CONTRIBUTOR(EngagementTrack.BUYER, "Community Contributor", "Published three community posts."),
    FIRST_LISTING(EngagementTrack.SELLER, "First Listing", "Published a first marketplace listing."),
    FIRST_SALE(EngagementTrack.SELLER, "First Sale", "Completed a first marketplace sale."),
    ESTABLISHED_SELLER(EngagementTrack.SELLER, "Established Seller", "Completed ten marketplace sales.");

    private final EngagementTrack track;
    private final String displayName;
    private final String description;

    BadgeCode(EngagementTrack track, String displayName, String description) {
        this.track = track;
        this.displayName = displayName;
        this.description = description;
    }

    public EngagementTrack getTrack() { return track; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
