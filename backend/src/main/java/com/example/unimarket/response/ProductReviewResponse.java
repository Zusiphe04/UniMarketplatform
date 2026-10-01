package com.example.unimarket.response;

import com.example.unimarket.domain.ProductReview;
import java.time.Instant;
import java.util.UUID;

public record ProductReviewResponse(
        UUID id, UUID productId, UUID reviewerId, String reviewerDisplayName,
        int rating, String comment, boolean verifiedPurchase,
        String sellerResponse, Instant sellerRespondedAt,
        Instant createdAt, Instant updatedAt
) {
    public static ProductReviewResponse from(ProductReview review, String displayName) {
        return new ProductReviewResponse(review.getId(), review.getProductId(), review.getReviewerId(),
                displayName, review.getRating(), review.getComment(), true,
                review.getSellerResponse(), review.getSellerRespondedAt(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}
