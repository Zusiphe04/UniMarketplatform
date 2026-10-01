package com.example.unimarket.factory;

import com.example.unimarket.domain.ProductReview;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class ProductReviewFactory {
    private ProductReviewFactory() { }
    public static ProductReview create(UUID productId, UUID reviewerId, int rating, String comment) {
        String text = Helper.cleanText(comment);
        if (productId == null || reviewerId == null || rating < 1 || rating > 5
                || text == null || text.length() > 1000) return null;
        return new ProductReview.Builder().setId(Helper.generateId()).setProductId(productId)
                .setReviewerId(reviewerId).setRating(rating).setComment(text).build();
    }
    public static ProductReview update(ProductReview review, int rating, String comment) {
        String text = Helper.cleanText(comment);
        return review != null && text != null && text.length() <= 1000 && review.update(rating, text)
                ? review : null;
    }
    public static ProductReview respond(ProductReview review, String response) {
        String text = Helper.cleanText(response);
        return review != null && text != null && text.length() <= 1000 && review.respond(text, Instant.now())
                ? review : null;
    }
}
