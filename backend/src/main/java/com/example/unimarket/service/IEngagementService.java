package com.example.unimarket.service;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductReview;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.response.EngagementProfileResponse;
import com.example.unimarket.response.LeaderboardEntryResponse;
import com.example.unimarket.response.LoyaltyPointResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IEngagementService {
    void recordCompletedOrder(MarketplaceOrder order, List<OrderItem> items);
    void recordReviewCreated(ProductReview review);
    void recordProductPublished(Product product);
    void recordBulletinPublished(BulletinPost post);
    EngagementProfileResponse getProfile(UUID userId);
    Page<LoyaltyPointResponse> listPoints(UUID userId, int page, int size);
    Page<LeaderboardEntryResponse> leaderboard(EngagementTrack track, int page, int size);
}
