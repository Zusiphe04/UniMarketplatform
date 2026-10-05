package com.example.unimarket.response;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.domain.enums.ProductCondition;
import com.example.unimarket.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Complete frontend-safe product listing contract. */
public record ProductResponse(
        UUID id,
        UUID sellerId,
        SellerSummaryResponse seller,
        String title,
        String description,
        ListingCategory category,
        ProductCondition condition,
        BigDecimal price,
        String currency,
        int quantity,
        int stockQuantity,
        int reservedQuantity,
        String brand,
        String model,
        String storage,
        String memory,
        String processor,
        String screenSize,
        String color,
        String size,
        ProductStatus status,
        List<ProductImageResponse> images,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) { return from(product, List.of(), null); }
    public static ProductResponse from(Product product, List<ProductImage> images) { return from(product, images, null); }

    public static ProductResponse from(Product product, List<ProductImage> images, SellerSummaryResponse seller) {
        if (product == null) return null;
        List<ProductImageResponse> media = images == null ? List.of()
                : images.stream().map(ProductImageResponse::from).toList();
        return new ProductResponse(product.getId(), product.getSellerId(), seller, product.getTitle(),
                product.getDescription(), ListingCategory.fromPersistenceCategory(product.getCategory()),
                product.getCondition(), product.getPrice(), product.getCurrency(), product.getAvailableQuantity(),
                product.getQuantity(), product.getReservedQuantity(), product.getBrand(), product.getModel(), product.getStorage(), product.getMemory(),
                product.getProcessor(), product.getScreenSize(), product.getColor(), product.getSize(),
                product.getStatus(), media, product.getPublishedAt(), product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
