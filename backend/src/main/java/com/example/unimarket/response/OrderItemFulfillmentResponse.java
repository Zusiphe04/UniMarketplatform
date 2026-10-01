package com.example.unimarket.response;

import com.example.unimarket.domain.OrderItemFulfillment;
import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderItemFulfillmentResponse(UUID id, UUID orderItemId, UUID orderId, UUID sellerId,
                                           OrderItemFulfillmentStatus status, String pickupInstructions,
                                           String trackingReference, Instant acceptedAt, Instant readyAt,
                                           Instant dispatchedAt, Instant receivedAt) {
    public static OrderItemFulfillmentResponse from(OrderItemFulfillment value) {
        return new OrderItemFulfillmentResponse(value.getId(), value.getOrderItemId(), value.getOrderId(),
                value.getSellerId(), value.getStatus(), value.getPickupInstructions(), value.getTrackingReference(),
                value.getAcceptedAt(), value.getReadyAt(), value.getDispatchedAt(), value.getReceivedAt());
    }
}
