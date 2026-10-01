package com.example.unimarket.response;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SellerOrderResponse(UUID orderId, String orderReference, OrderStatus status,
                                  BigDecimal sellerTotal, String currency,
                                  List<OrderItemResponse> items, Instant placedAt, Instant paidAt,
                                  Instant heldAt, Instant disputedAt, Instant releasedAt, Instant refundedAt) {
    public static SellerOrderResponse from(MarketplaceOrder order, List<OrderItem> sellerItems) {
        BigDecimal total = sellerItems.stream().map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new SellerOrderResponse(order.getId(), order.getReference(), order.getStatus(), total,
                order.getCurrency(), sellerItems.stream().map(OrderItemResponse::from).toList(),
                order.getPlacedAt(), order.getPaidAt(), order.getHeldAt(), order.getDisputedAt(),
                order.getReleasedAt(), order.getRefundedAt());
    }
}
