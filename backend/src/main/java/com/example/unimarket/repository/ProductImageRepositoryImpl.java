package com.example.unimarket.repository;

import com.example.unimarket.domain.ProductImage;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class ProductImageRepositoryImpl implements IProductImageRepository {
    private static volatile ProductImageRepositoryImpl instance;
    private final ProductImageJpaRepository jpaRepository;
    public ProductImageRepositoryImpl(ProductImageJpaRepository repository) {
        jpaRepository = repository;
        if (instance == null) instance = this;
    }
    public static ProductImageRepositoryImpl getInstance(ProductImageJpaRepository repository) {
        if (instance == null) synchronized (ProductImageRepositoryImpl.class) {
            if (instance == null) instance = new ProductImageRepositoryImpl(repository);
        }
        return instance;
    }
    @Override public ProductImage create(ProductImage value) { return value == null ? null : jpaRepository.save(value); }
    @Override public ProductImage read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public ProductImage update(ProductImage value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<ProductImage> getAll() { return jpaRepository.findAll(); }
    @Override public List<ProductImage> readByProductId(UUID productId) {
        return productId == null ? List.of() : jpaRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }
    @Override public List<ProductImage> readByProductIds(Collection<UUID> ids) {
        return ids == null || ids.isEmpty() ? List.of()
                : jpaRepository.findByProductIdInOrderByProductIdAscDisplayOrderAsc(ids);
    }
    @Override public long countByProductId(UUID id) { return id == null ? 0 : jpaRepository.countByProductId(id); }
}
