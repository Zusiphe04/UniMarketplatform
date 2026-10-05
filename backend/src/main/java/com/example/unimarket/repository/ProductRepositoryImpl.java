package com.example.unimarket.repository;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ProductCategory;
import com.example.unimarket.domain.enums.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Singleton implementation of {@link IProductRepository}. */
@Repository
public class ProductRepositoryImpl implements IProductRepository {

    private static final Set<ProductStatus> PUBLIC_STATUSES =
            Set.of(ProductStatus.PUBLISHED, ProductStatus.SOLD_OUT);

    private static volatile ProductRepositoryImpl instance;

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public static ProductRepositoryImpl getInstance(ProductJpaRepository jpaRepository) {
        if (instance == null) {
            synchronized (ProductRepositoryImpl.class) {
                if (instance == null) {
                    instance = new ProductRepositoryImpl(jpaRepository);
                }
            }
        }
        return instance;
    }

    @Override
    public Product create(Product entity) {
        return entity == null ? null : jpaRepository.save(entity);
    }

    @Override
    public Product read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }

    @Override
    public Product update(Product entity) {
        if (entity == null || entity.getId() == null || !jpaRepository.existsById(entity.getId())) {
            return null;
        }
        return jpaRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Product> getAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Product readByIdForUpdate(UUID id) {
        return id == null ? null : jpaRepository.findByIdForUpdate(id).orElse(null);
    }

    @Override
    public Product readPublicById(UUID id) {
        return id == null ? null : jpaRepository.findByIdAndStatusIn(id, PUBLIC_STATUSES).orElse(null);
    }

    @Override
    public Page<Product> readBySellerId(UUID sellerId, Pageable pageable) {
        return jpaRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
    }

    @Override
    public Page<Product> searchPublic(String query,
                                      Collection<ProductCategory> categories,
                                      Pageable pageable) {
        return jpaRepository.searchPublic(PUBLIC_STATUSES, query, categories, pageable);
    }
}
