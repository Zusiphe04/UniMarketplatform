package com.example.unimarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_product_reviewer", columnNames = {"product_id", "reviewer_id"}),
        indexes = @Index(name = "idx_review_product_created", columnList = "product_id, created_at"))
public class ProductReview extends AuditableEntity {
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "reviewer_id", nullable = false) private UUID reviewerId;
    @Column(name = "rating", nullable = false) private int rating;
    @Column(name = "comment", nullable = false, length = 1000) private String comment;
    @Column(name = "seller_response", length = 1000) private String sellerResponse;
    @Column(name = "seller_responded_at") private Instant sellerRespondedAt;

    protected ProductReview() { super(); }
    private ProductReview(Builder builder) {
        super(builder.id); productId = builder.productId; reviewerId = builder.reviewerId;
        rating = builder.rating; comment = builder.comment; sellerResponse = builder.sellerResponse;
        sellerRespondedAt = builder.sellerRespondedAt;
    }
    public UUID getProductId() { return productId; }
    public UUID getReviewerId() { return reviewerId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getSellerResponse() { return sellerResponse; }
    public Instant getSellerRespondedAt() { return sellerRespondedAt; }
    public boolean update(int value, String text) {
        if (value < 1 || value > 5 || text == null) return false;
        rating = value; comment = text; return true;
    }
    public boolean respond(String response, Instant at) {
        if (response == null || response.isBlank() || at == null
                || sellerResponse != null || sellerRespondedAt != null) return false;
        sellerResponse = response;
        sellerRespondedAt = at;
        return true;
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof ProductReview value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID productId; private UUID reviewerId; private int rating; private String comment;
        private String sellerResponse; private Instant sellerRespondedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setProductId(UUID value) { productId = value; return this; }
        public Builder setReviewerId(UUID value) { reviewerId = value; return this; }
        public Builder setRating(int value) { rating = value; return this; }
        public Builder setComment(String value) { comment = value; return this; }
        public Builder setSellerResponse(String value) { sellerResponse = value; return this; }
        public Builder setSellerRespondedAt(Instant value) { sellerRespondedAt = value; return this; }
        public Builder copy(ProductReview value) { return setId(value.getId()).setProductId(value.getProductId())
                .setReviewerId(value.getReviewerId()).setRating(value.getRating()).setComment(value.getComment())
                .setSellerResponse(value.getSellerResponse()).setSellerRespondedAt(value.getSellerRespondedAt()); }
        public ProductReview build() { return new ProductReview(this); }
    }
}
