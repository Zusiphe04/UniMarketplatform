package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductImage;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IProductImageRepository extends IRepository<ProductImage, UUID> {
    List<ProductImage> readByProductId(UUID productId);
    List<ProductImage> readByProductIds(Collection<UUID> productIds);
    long countByProductId(UUID productId);
}
