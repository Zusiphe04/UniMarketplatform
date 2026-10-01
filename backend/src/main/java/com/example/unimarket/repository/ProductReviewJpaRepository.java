package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductReviewJpaRepository extends JpaRepository<ProductReview, UUID> {
    Page<ProductReview> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);
    Optional<ProductReview> findByProductIdAndReviewerId(UUID productId, UUID reviewerId);
    long countByProductId(UUID productId);
    @Query("select avg(r.rating) from ProductReview r where r.productId = :productId")
    Double averageRating(@Param("productId") UUID productId);
}
