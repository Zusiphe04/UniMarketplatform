package com.example.unimarket.repository;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ProductCategory;
import com.example.unimarket.domain.enums.ProductStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA access to the {@code product} table. */
public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);

    Optional<Product> findByIdAndStatusIn(UUID id, Collection<ProductStatus> statuses);

    @Query("""
            select product
            from Product product
            where product.status in :statuses
              and product.category in :categories
              and (:query is null
                   or lower(product.title) like lower(concat('%', :query, '%'))
                   or lower(product.description) like lower(concat('%', :query, '%')))
            order by product.publishedAt desc, product.id desc
            """)
    Page<Product> searchPublic(@Param("statuses") Collection<ProductStatus> statuses,
                               @Param("query") String query,
                               @Param("categories") Collection<ProductCategory> categories,
                               Pageable pageable);
}
