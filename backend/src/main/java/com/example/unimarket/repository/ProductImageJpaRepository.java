package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductImageJpaRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(UUID productId);
    List<ProductImage> findByProductIdInOrderByProductIdAscDisplayOrderAsc(Collection<UUID> productIds);
    long countByProductId(UUID productId);
}
