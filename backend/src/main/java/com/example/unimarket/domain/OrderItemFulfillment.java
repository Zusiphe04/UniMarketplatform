package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Seller and buyer operational lifecycle for one immutable order item. */
@Entity
@Table(name = "order_item_fulfillment",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_item_fulfillment_item", columnNames = "order_item_id"),
        indexes = {
                @Index(name = "idx_fulfillment_order", columnList = "order_id, fulfillment_status"),
                @Index(name = "idx_fulfillment_seller", columnList = "seller_id, fulfillment_status")
        })
public class OrderItemFulfillment extends AuditableEntity {
    @Column(name = "order_item_id", nullable = false) private UUID orderItemId;
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Column(name = "seller_id", nullable = false) private UUID sellerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false, length = 30)
    private OrderItemFulfillmentStatus status;
    @Column(name = "pickup_instructions", length = 500) private String pickupInstructions;
    @Column(name = "tracking_reference", length = 160) private String trackingReference;
    @Column(name = "accepted_at") private Instant acceptedAt;
    @Column(name = "ready_at") private Instant readyAt;
    @Column(name = "dispatched_at") private Instant dispatchedAt;
    @Column(name = "received_at") private Instant receivedAt;

    protected OrderItemFulfillment() { super(); }
    private OrderItemFulfillment(Builder builder) {
        super(builder.id); orderItemId = builder.orderItemId; orderId = builder.orderId; sellerId = builder.sellerId;
        status = builder.status; pickupInstructions = builder.pickupInstructions; trackingReference = builder.trackingReference;
        acceptedAt = builder.acceptedAt; readyAt = builder.readyAt; dispatchedAt = builder.dispatchedAt; receivedAt = builder.receivedAt;
    }
    public UUID getOrderItemId() { return orderItemId; }
    public UUID getOrderId() { return orderId; }
    public UUID getSellerId() { return sellerId; }
    public OrderItemFulfillmentStatus getStatus() { return status; }
    public String getPickupInstructions() { return pickupInstructions; }
    public String getTrackingReference() { return trackingReference; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getReadyAt() { return readyAt; }
    public Instant getDispatchedAt() { return dispatchedAt; }
    public Instant getReceivedAt() { return receivedAt; }

    public boolean updateBySeller(OrderItemFulfillmentStatus next, String pickup, String tracking, Instant now) {
        if (next == null || now == null) return false;
        if (next == OrderItemFulfillmentStatus.ACCEPTED && status == OrderItemFulfillmentStatus.AWAITING_SELLER) {
            status = next; acceptedAt = now; return true;
        }
        if (next == OrderItemFulfillmentStatus.READY_FOR_COLLECTION && status == OrderItemFulfillmentStatus.ACCEPTED) {
            status = next; readyAt = now; pickupInstructions = pickup; return true;
        }
        if (next == OrderItemFulfillmentStatus.DISPATCHED && status == OrderItemFulfillmentStatus.ACCEPTED) {
            status = next; dispatchedAt = now; trackingReference = tracking; return true;
        }
        return false;
    }

    public boolean confirmReceived(Instant now) {
        if ((status != OrderItemFulfillmentStatus.READY_FOR_COLLECTION && status != OrderItemFulfillmentStatus.DISPATCHED)
                || now == null) return false;
        status = OrderItemFulfillmentStatus.RECEIVED;
        receivedAt = now;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof OrderItemFulfillment value && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id; private UUID orderItemId; private UUID orderId; private UUID sellerId;
        private OrderItemFulfillmentStatus status; private String pickupInstructions; private String trackingReference;
        private Instant acceptedAt; private Instant readyAt; private Instant dispatchedAt; private Instant receivedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setOrderItemId(UUID value) { orderItemId = value; return this; }
        public Builder setOrderId(UUID value) { orderId = value; return this; }
        public Builder setSellerId(UUID value) { sellerId = value; return this; }
        public Builder setStatus(OrderItemFulfillmentStatus value) { status = value; return this; }
        public Builder setPickupInstructions(String value) { pickupInstructions = value; return this; }
        public Builder setTrackingReference(String value) { trackingReference = value; return this; }
        public Builder setAcceptedAt(Instant value) { acceptedAt = value; return this; }
        public Builder setReadyAt(Instant value) { readyAt = value; return this; }
        public Builder setDispatchedAt(Instant value) { dispatchedAt = value; return this; }
        public Builder setReceivedAt(Instant value) { receivedAt = value; return this; }
        public OrderItemFulfillment build() { return new OrderItemFulfillment(this); }
    }
}
