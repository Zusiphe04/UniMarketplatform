package com.example.unimarket.repository;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ProductCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.UUID;

/** Persistence contract for seller-owned marketplace products. */
public interface IProductRepository extends IRepository<Product, UUID> {

    /** Reads a product while holding a database write lock for a managed transition. */
    Product readByIdForUpdate(UUID id);

    /** Reads a product only when it is visible in the public catalogue. */
    Product readPublicById(UUID id);

    /** Lists every lifecycle state owned by one seller, newest first. */
    Page<Product> readBySellerId(UUID sellerId, Pageable pageable);

    /** Searches products visible in the public catalogue using persisted compatibility values. */
    Page<Product> searchPublic(String query, Collection<ProductCategory> categories, Pageable pageable);
}
