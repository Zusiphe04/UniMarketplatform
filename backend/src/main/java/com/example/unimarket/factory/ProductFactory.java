package com.example.unimarket.factory;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.domain.enums.ProductCondition;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.util.Helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/** Creates and transitions valid {@link Product} aggregates. */
public final class ProductFactory {

    public static final String CURRENCY = "ZAR";

    private ProductFactory() {
        // Factory class.
    }

    public static Product create(UUID sellerId,
                                 String title,
                                 String description,
                                 ListingCategory category,
                                 ProductCondition condition,
                                 BigDecimal price,
                                 int quantity,
                                 String brand,
                                 String model,
                                 String storage,
                                 String memory,
                                 String processor,
                                 String screenSize,
                                 String color,
                                 String size) {
        String cleanedTitle = cleanBounded(title, 160);
        String cleanedDescription = cleanBounded(description, 4000);
        BigDecimal normalizedPrice = normalizePositiveMoney(price);
        if (!hasValidSpecificationLengths(brand, model, storage, memory, processor,
                screenSize, color, size)) {
            return null;
        }
        String cleanedBrand = cleanOptional(brand);
        String cleanedModel = cleanOptional(model);
        String cleanedStorage = cleanOptional(storage);
        String cleanedMemory = cleanOptional(memory);
        String cleanedProcessor = cleanOptional(processor);
        String cleanedScreenSize = cleanOptional(screenSize);
        String cleanedColor = cleanOptional(color);
        String cleanedSize = cleanOptional(size);
        if (sellerId == null || cleanedTitle == null || cleanedDescription == null
                || category == null || condition == null || normalizedPrice == null
                || quantity < 1 || !hasValidCondition(category, condition)
                || !hasValidSpecifications(category, cleanedBrand, cleanedModel, cleanedStorage,
                        cleanedMemory, cleanedProcessor, cleanedScreenSize, cleanedColor, cleanedSize)) {
            return null;
        }
        return new Product.Builder()
                .setId(Helper.generateId())
                .setSellerId(sellerId)
                .setTitle(cleanedTitle)
                .setDescription(cleanedDescription)
                .setCategory(category.persistenceCategory())
                .setCondition(condition)
                .setPrice(normalizedPrice)
                .setCurrency(CURRENCY)
                .setQuantity(quantity)
                .setBrand(cleanedBrand)
                .setModel(cleanedModel)
                .setStorage(cleanedStorage)
                .setMemory(cleanedMemory)
                .setProcessor(cleanedProcessor)
                .setScreenSize(cleanedScreenSize)
                .setColor(cleanedColor)
                .setSize(cleanedSize)
                .setStatus(ProductStatus.DRAFT)
                .setPublishedAt(null)
                .build();
    }

    public static Product update(Product product,
                                 String title,
                                 String description,
                                 ListingCategory category,
                                 ProductCondition condition,
                                 BigDecimal price,
                                 int quantity,
                                 String brand,
                                 String model,
                                 String storage,
                                 String memory,
                                 String processor,
                                 String screenSize,
                                 String color,
                                 String size) {
        String cleanedTitle = cleanBounded(title, 160);
        String cleanedDescription = cleanBounded(description, 4000);
        BigDecimal normalizedPrice = normalizePositiveMoney(price);
        if (!hasValidSpecificationLengths(brand, model, storage, memory, processor,
                screenSize, color, size)) {
            return null;
        }
        String cleanedBrand = cleanOptional(brand);
        String cleanedModel = cleanOptional(model);
        String cleanedStorage = cleanOptional(storage);
        String cleanedMemory = cleanOptional(memory);
        String cleanedProcessor = cleanOptional(processor);
        String cleanedScreenSize = cleanOptional(screenSize);
        String cleanedColor = cleanOptional(color);
        String cleanedSize = cleanOptional(size);
        if (product == null || cleanedTitle == null || cleanedDescription == null
                || category == null || condition == null || normalizedPrice == null
                || quantity < 0 || !hasValidCondition(category, condition)
                || !hasValidSpecifications(category, cleanedBrand, cleanedModel, cleanedStorage,
                        cleanedMemory, cleanedProcessor, cleanedScreenSize, cleanedColor, cleanedSize)
                || !product.updateDetails(cleanedTitle, cleanedDescription, category.persistenceCategory(),
                        condition, normalizedPrice, quantity, cleanedBrand, cleanedModel, cleanedStorage,
                        cleanedMemory, cleanedProcessor, cleanedScreenSize, cleanedColor, cleanedSize)) {
            return null;
        }
        return product;
    }

    public static Product publish(Product product) {
        return product != null && product.publish(Instant.now()) ? product : null;
    }

    public static Product archive(Product product) {
        return product != null && product.archive() ? product : null;
    }

    public static Product reserveStock(Product product, int quantity) {
        return product != null && product.reserveStock(quantity) ? product : null;
    }

    public static Product releaseReservedStock(Product product, int quantity) {
        return product != null && product.releaseReservedStock(quantity) ? product : null;
    }

    public static Product consumeReservedStock(Product product, int quantity) {
        return product != null && product.consumeReservedStock(quantity) ? product : null;
    }

    public static Product decrementStock(Product product, int quantity) {
        return product != null && product.decrementStock(quantity) ? product : null;
    }

    private static String cleanBounded(String value, int maxLength) {
        String cleaned = Helper.cleanText(value);
        return cleaned != null && cleaned.length() <= maxLength ? cleaned : null;
    }

    private static String cleanOptional(String value) {
        return Helper.cleanText(value);
    }

    private static boolean hasValidSpecificationLengths(String brand,
                                                        String model,
                                                        String storage,
                                                        String memory,
                                                        String processor,
                                                        String screenSize,
                                                        String color,
                                                        String size) {
        return isOptionalBounded(brand, 80)
                && isOptionalBounded(model, 120)
                && isOptionalBounded(storage, 80)
                && isOptionalBounded(memory, 80)
                && isOptionalBounded(processor, 120)
                && isOptionalBounded(screenSize, 80)
                && isOptionalBounded(color, 80)
                && isOptionalBounded(size, 80);
    }

    private static boolean isOptionalBounded(String value, int maxLength) {
        String cleaned = Helper.cleanText(value);
        return cleaned == null || cleaned.length() <= maxLength;
    }

    private static BigDecimal normalizePositiveMoney(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return null;
        }
        try {
            BigDecimal normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
            return normalized.precision() <= 19 ? normalized : null;
        } catch (ArithmeticException exception) {
            return null;
        }
    }

    private static boolean hasValidCondition(ListingCategory category, ProductCondition condition) {
        return category.isService()
                ? condition == ProductCondition.NOT_APPLICABLE
                : condition != ProductCondition.NOT_APPLICABLE;
    }

    private static boolean hasValidSpecifications(ListingCategory category,
                                                  String brand,
                                                  String model,
                                                  String storage,
                                                  String memory,
                                                  String processor,
                                                  String screenSize,
                                                  String color,
                                                  String size) {
        return switch (category) {
            case TECH -> size == null;
            case CLOTHING -> model == null && storage == null && memory == null
                    && processor == null && screenSize == null;
            case ROOM_AND_HOME, OTHER -> storage == null && memory == null
                    && processor == null && screenSize == null;
            case BOOKS, SERVICE -> brand == null && model == null && storage == null
                    && memory == null && processor == null && screenSize == null
                    && color == null && size == null;
        };
    }
}
