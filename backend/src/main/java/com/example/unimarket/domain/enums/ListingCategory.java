package com.example.unimarket.domain.enums;

import java.util.Set;

/** Canonical marketplace categories exposed by the product API and seller UI. */
public enum ListingCategory {
    SERVICE(ProductCategory.SERVICES),
    TECH(ProductCategory.ELECTRONICS),
    BOOKS(ProductCategory.BOOKS),
    CLOTHING(ProductCategory.FASHION),
    ROOM_AND_HOME(ProductCategory.HOME),
    OTHER(ProductCategory.OTHER, ProductCategory.SPORTS);

    private final ProductCategory persistenceCategory;
    private final Set<ProductCategory> persistenceCategories;

    ListingCategory(ProductCategory persistenceCategory, ProductCategory... compatibleCategories) {
        this.persistenceCategory = persistenceCategory;
        ProductCategory[] categories = new ProductCategory[compatibleCategories.length + 1];
        categories[0] = persistenceCategory;
        System.arraycopy(compatibleCategories, 0, categories, 1, compatibleCategories.length);
        this.persistenceCategories = Set.of(categories);
    }

    /** Existing database enum value used for new writes. */
    public ProductCategory persistenceCategory() {
        return persistenceCategory;
    }

    /** Existing database enum values represented by this public category. */
    public Set<ProductCategory> persistenceCategories() {
        return persistenceCategories;
    }

    public boolean isService() {
        return this == SERVICE;
    }

    public static ListingCategory fromPersistenceCategory(ProductCategory category) {
        if (category == null) return null;
        return switch (category) {
            case BOOKS -> BOOKS;
            case ELECTRONICS -> TECH;
            case FASHION -> CLOTHING;
            case HOME -> ROOM_AND_HOME;
            case SERVICES -> SERVICE;
            case SPORTS, OTHER -> OTHER;
        };
    }
}
