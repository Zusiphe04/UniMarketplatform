package com.example.unimarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Immutable price/title snapshot for one purchased product. */
@Entity
@Table(name = "order_item", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id"),
        @Index(name = "idx_order_item_seller", columnList = "seller_id, order_id"),
        @Index(name = "idx_order_item_product", columnList = "product_id, order_id")
})
public class OrderItem extends AuditableEntity {
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "seller_id", nullable = false) private UUID sellerId;
    @Column(name = "product_title", nullable = false, length = 160) private String productTitle;
    @Column(name = "quantity", nullable = false) private int quantity;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2) private BigDecimal unitPrice;
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2) private BigDecimal subtotal;
    @Column(name = "currency", nullable = false, length = 3) private String currency;

    protected OrderItem() { super(); }
    private OrderItem(Builder builder) {
        super(builder.id); orderId = builder.orderId; productId = builder.productId;
        sellerId = builder.sellerId; productTitle = builder.productTitle; quantity = builder.quantity;
        unitPrice = builder.unitPrice; subtotal = builder.subtotal; currency = builder.currency;
    }
    public UUID getOrderId() { return orderId; }
    public UUID getProductId() { return productId; }
    public UUID getSellerId() { return sellerId; }
    public String getProductTitle() { return productTitle; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getCurrency() { return currency; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof OrderItem value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID orderId; private UUID productId; private UUID sellerId;
        private String productTitle; private int quantity; private BigDecimal unitPrice;
        private BigDecimal subtotal; private String currency;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setOrderId(UUID value) { orderId = value; return this; }
        public Builder setProductId(UUID value) { productId = value; return this; }
        public Builder setSellerId(UUID value) { sellerId = value; return this; }
        public Builder setProductTitle(String value) { productTitle = value; return this; }
        public Builder setQuantity(int value) { quantity = value; return this; }
        public Builder setUnitPrice(BigDecimal value) { unitPrice = value; return this; }
        public Builder setSubtotal(BigDecimal value) { subtotal = value; return this; }
        public Builder setCurrency(String value) { currency = value; return this; }
        public OrderItem build() { return new OrderItem(this); }
    }
}
