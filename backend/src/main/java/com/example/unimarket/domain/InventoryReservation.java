package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.InventoryReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** A per-product stock hold owned by an awaiting-payment order. */
@Entity
@Table(name = "inventory_reservation",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_reservation_order_product",
                columnNames = {"order_id", "product_id"}),
        indexes = {
                @Index(name = "idx_inventory_reservation_expiry", columnList = "reservation_status, expires_at"),
                @Index(name = "idx_inventory_reservation_order", columnList = "order_id, reservation_status"),
                @Index(name = "idx_inventory_reservation_product", columnList = "product_id, reservation_status")
        })
public class InventoryReservation extends AuditableEntity {
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "quantity", nullable = false) private int quantity;
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private InventoryReservationStatus status;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;

    protected InventoryReservation() { super(); }

    private InventoryReservation(Builder builder) {
        super(builder.id);
        orderId = builder.orderId;
        productId = builder.productId;
        quantity = builder.quantity;
        status = builder.status;
        expiresAt = builder.expiresAt;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public InventoryReservationStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return status == InventoryReservationStatus.ACTIVE; }
    public boolean consume() { return transitionTo(InventoryReservationStatus.CONSUMED); }
    public boolean release() { return transitionTo(InventoryReservationStatus.RELEASED); }
    public boolean expire() { return transitionTo(InventoryReservationStatus.EXPIRED); }

    private boolean transitionTo(InventoryReservationStatus target) {
        if (!isActive() || target == null) return false;
        status = target;
        return true;
    }

    public static class Builder {
        private UUID id;
        private UUID orderId;
        private UUID productId;
        private int quantity;
        private InventoryReservationStatus status;
        private Instant expiresAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setOrderId(UUID value) { orderId = value; return this; }
        public Builder setProductId(UUID value) { productId = value; return this; }
        public Builder setQuantity(int value) { quantity = value; return this; }
        public Builder setStatus(InventoryReservationStatus value) { status = value; return this; }
        public Builder setExpiresAt(Instant value) { expiresAt = value; return this; }
        public InventoryReservation build() { return new InventoryReservation(this); }
    }
}
