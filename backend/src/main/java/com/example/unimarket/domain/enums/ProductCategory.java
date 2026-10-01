package com.example.unimarket.domain.enums;

/**
 * Legacy product-table enum values retained so existing MySQL rows remain readable.
 * New API requests and responses use {@link ListingCategory}.
 */
public enum ProductCategory {
    BOOKS,
    ELECTRONICS,
    FASHION,
    HOME,
    SPORTS,
    SERVICES,
    OTHER
}
