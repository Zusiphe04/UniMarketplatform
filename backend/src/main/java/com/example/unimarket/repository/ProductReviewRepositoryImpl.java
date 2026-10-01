package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ProductReviewRepositoryImpl implements IProductReviewRepository {
    private final ProductReviewJpaRepository jpaRepository;
    public ProductReviewRepositoryImpl(ProductReviewJpaRepository repository) { jpaRepository = repository; }
    @Override public ProductReview create(ProductReview value) { return value == null ? null : jpaRepository.save(value); }
    @Override public ProductReview read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public ProductReview update(ProductReview value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<ProductReview> getAll() { return jpaRepository.findAll(); }
    @Override public Page<ProductReview> readByProductId(UUID id, Pageable pageable) {
        return jpaRepository.findByProductIdOrderByCreatedAtDesc(id, pageable);
    }
    @Override public ProductReview readByProductIdAndReviewerId(UUID productId, UUID reviewerId) {
        return jpaRepository.findByProductIdAndReviewerId(productId, reviewerId).orElse(null);
    }
    @Override public Double averageRating(UUID id) { return jpaRepository.averageRating(id); }
    @Override public long countByProductId(UUID id) { return jpaRepository.countByProductId(id); }
}
