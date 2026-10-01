package com.example.unimarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cart_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_buyer_product", columnNames = {"buyer_id", "product_id"}),
        indexes = @Index(name = "idx_cart_buyer", columnList = "buyer_id, added_at"))
public class CartItem extends AuditableEntity {
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "quantity", nullable = false) private int quantity;
    @Column(name = "added_at", nullable = false) private Instant addedAt;

    protected CartItem() { super(); }
    private CartItem(Builder builder) {
        super(builder.id); buyerId = builder.buyerId; productId = builder.productId;
        quantity = builder.quantity; addedAt = builder.addedAt;
    }
    public UUID getBuyerId() { return buyerId; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Instant getAddedAt() { return addedAt; }
    public boolean changeQuantity(int value) {
        if (value < 1 || value > 99) return false;
        quantity = value; return true;
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof CartItem value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID buyerId; private UUID productId; private int quantity; private Instant addedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setBuyerId(UUID value) { buyerId = value; return this; }
        public Builder setProductId(UUID value) { productId = value; return this; }
        public Builder setQuantity(int value) { quantity = value; return this; }
        public Builder setAddedAt(Instant value) { addedAt = value; return this; }
        public Builder copy(CartItem value) { return setId(value.getId()).setBuyerId(value.getBuyerId())
                .setProductId(value.getProductId()).setQuantity(value.getQuantity()).setAddedAt(value.getAddedAt()); }
        public CartItem build() { return new CartItem(this); }
    }
}
