package com.example.unimarket.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record ProductRatingSummaryResponse(UUID productId, BigDecimal averageRating, long reviewCount) {
    public static ProductRatingSummaryResponse of(UUID productId, Double average, long count) {
        BigDecimal value = average == null ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        return new ProductRatingSummaryResponse(productId, value, count);
    }
}
