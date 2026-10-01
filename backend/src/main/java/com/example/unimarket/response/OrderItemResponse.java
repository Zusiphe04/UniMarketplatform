package com.example.unimarket.response;

import com.example.unimarket.domain.OrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(UUID id, UUID productId, UUID sellerId, String productTitle,
                                int quantity, BigDecimal unitPrice, BigDecimal subtotal, String currency) {
    public static OrderItemResponse from(OrderItem value) {
        return new OrderItemResponse(value.getId(), value.getProductId(), value.getSellerId(),
                value.getProductTitle(), value.getQuantity(), value.getUnitPrice(), value.getSubtotal(),
                value.getCurrency());
    }
}
