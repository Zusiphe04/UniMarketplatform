package com.example.unimarket.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartItemResponse> items, int itemCount,
                           BigDecimal estimatedTotal, String currency) {
    public static CartResponse of(List<CartItemResponse> items) {
        List<CartItemResponse> safe = items == null ? List.of() : List.copyOf(items);
        int count = safe.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal total = safe.stream().map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new CartResponse(safe, count, total, "ZAR");
    }
}
