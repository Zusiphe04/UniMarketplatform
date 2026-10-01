package com.example.unimarket.response;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.enums.FulfillmentMethod;
import com.example.unimarket.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id, String reference, UUID buyerId, BigDecimal totalAmount, String currency,
        int itemCount, OrderStatus status, FulfillmentMethod fulfillmentMethod,
        String deliveryAddress, List<OrderItemResponse> items, Instant placedAt,
        Instant paidAt, Instant heldAt, Instant disputedAt, Instant releasedAt, Instant refundedAt,
        String paymentFailureCode, boolean reservationActive, Instant reservationExpiresAt
) {
    public static OrderResponse from(MarketplaceOrder order, List<OrderItem> items) {
        List<OrderItemResponse> responses = items == null ? List.of()
                : items.stream().map(OrderItemResponse::from).toList();
        return new OrderResponse(order.getId(), order.getReference(), order.getBuyerId(),
                order.getTotalAmount(), order.getCurrency(), order.getItemCount(), order.getStatus(),
                order.getFulfillmentMethod(), order.getDeliveryAddress(), responses, order.getPlacedAt(),
                order.getPaidAt(), order.getHeldAt(), order.getDisputedAt(), order.getReleasedAt(),
                order.getRefundedAt(), order.getPaymentFailureCode(), order.isReservationActive(),
                order.getReservationExpiresAt());
    }
}
