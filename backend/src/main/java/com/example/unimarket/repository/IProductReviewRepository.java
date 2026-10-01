package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProductReviewRepository extends IRepository<ProductReview, UUID> {
    Page<ProductReview> readByProductId(UUID productId, Pageable pageable);
    ProductReview readByProductIdAndReviewerId(UUID productId, UUID reviewerId);
    Double averageRating(UUID productId);
    long countByProductId(UUID productId);
}
