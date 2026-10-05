package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.ProductCategory;
import com.example.unimarket.domain.enums.ProductCondition;
import com.example.unimarket.domain.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A seller-owned item or service offered through the marketplace. */
@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_public_search", columnList = "status, category, published_at"),
        @Index(name = "idx_product_seller", columnList = "seller_id, status")
})
public class Product extends AuditableEntity {
    @Column(name = "seller_id", nullable = false) private UUID sellerId;
    @Column(name = "title", nullable = false, length = 160) private String title;
    @Column(name = "description", nullable = false, length = 4000) private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30) private ProductCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 30) private ProductCondition condition;
    @Column(name = "price", nullable = false, precision = 19, scale = 2) private BigDecimal price;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    /** Physical on-hand stock. It changes only when a payment consumes stock. */
    @Column(name = "quantity", nullable = false) private int quantity;
    /** Stock held by active checkout reservations. */
    @Column(name = "reserved_quantity", nullable = false) private int reservedQuantity;
    @Column(name = "brand", length = 80) private String brand;
    @Column(name = "product_model", length = 120) private String model;
    @Column(name = "storage_specification", length = 80) private String storage;
    @Column(name = "memory_specification", length = 80) private String memory;
    @Column(name = "processor", length = 120) private String processor;
    @Column(name = "screen_size", length = 80) private String screenSize;
    @Column(name = "color", length = 80) private String color;
    @Column(name = "item_size", length = 80) private String size;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) private ProductStatus status;
    @Column(name = "published_at") private Instant publishedAt;

    protected Product() { super(); }
    private Product(Builder builder) {
        super(builder.id); sellerId = builder.sellerId; title = builder.title; description = builder.description;
        category = builder.category; condition = builder.condition; price = builder.price; currency = builder.currency;
        quantity = builder.quantity; reservedQuantity = builder.reservedQuantity; brand = builder.brand; model = builder.model;
        storage = builder.storage; memory = builder.memory; processor = builder.processor; screenSize = builder.screenSize;
        color = builder.color; size = builder.size; status = builder.status; publishedAt = builder.publishedAt;
    }

    public UUID getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProductCategory getCategory() { return category; }
    public ProductCondition getCondition() { return condition; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public int getQuantity() { return quantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getAvailableQuantity() { return quantity - reservedQuantity; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getStorage() { return storage; }
    public String getMemory() { return memory; }
    public String getProcessor() { return processor; }
    public String getScreenSize() { return screenSize; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public ProductStatus getStatus() { return status; }
    public Instant getPublishedAt() { return publishedAt; }

    /** Applies seller-managed fields to a draft or live product when no stock is reserved. */
    public boolean updateDetails(String title, String description, ProductCategory category, ProductCondition condition,
                                 BigDecimal price, int quantity, String brand, String model, String storage,
                                 String memory, String processor, String screenSize, String color, String size) {
        if (status == ProductStatus.ARCHIVED || reservedQuantity != 0) return false;
        this.title = title; this.description = description; this.category = category; this.condition = condition;
        this.price = price; this.quantity = quantity; this.brand = brand; this.model = model; this.storage = storage;
        this.memory = memory; this.processor = processor; this.screenSize = screenSize; this.color = color; this.size = size;
        if (status == ProductStatus.PUBLISHED && quantity == 0) status = ProductStatus.SOLD_OUT;
        else if (status == ProductStatus.SOLD_OUT && quantity > 0) status = ProductStatus.PUBLISHED;
        return true;
    }

    public boolean publish(Instant at) {
        if (status != ProductStatus.DRAFT || quantity < 1 || reservedQuantity != 0 || at == null) return false;
        status = ProductStatus.PUBLISHED; publishedAt = at; return true;
    }
    public boolean archive() {
        if (status == ProductStatus.ARCHIVED) return false;
        status = ProductStatus.ARCHIVED; return true;
    }

    /** Holds currently available stock without mutating physical quantity. */
    public boolean reserveStock(int amount) {
        if (status != ProductStatus.PUBLISHED || amount < 1 || amount > getAvailableQuantity()) return false;
        reservedQuantity += amount;
        return true;
    }
    /** Releases an active reservation without changing physical quantity. */
    public boolean releaseReservedStock(int amount) {
        if (amount < 1 || amount > reservedQuantity) return false;
        reservedQuantity -= amount;
        return true;
    }
    /** Consumes a reservation during successful payment, decrementing physical stock exactly once. */
    public boolean consumeReservedStock(int amount) {
        if (amount < 1 || amount > reservedQuantity || amount > quantity) return false;
        reservedQuantity -= amount;
        quantity -= amount;
        if (quantity == 0 && status == ProductStatus.PUBLISHED) status = ProductStatus.SOLD_OUT;
        return true;
    }
    /** Supports only legacy, pre-reservation orders during a safe rollout. */
    public boolean decrementStock(int purchasedQuantity) {
        if (status != ProductStatus.PUBLISHED || purchasedQuantity < 1 || purchasedQuantity > getAvailableQuantity()) {
            return false;
        }
        quantity -= purchasedQuantity;
        if (quantity == 0) status = ProductStatus.SOLD_OUT;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof Product product && getId() != null && getId().equals(product.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    @Override public String toString() {
        return "Product{" + "id=" + getId() + ", sellerId=" + sellerId + ", title='" + title + '\''
                + ", status=" + status + '}';
    }

    public static class Builder {
        private UUID id; private UUID sellerId; private String title; private String description;
        private ProductCategory category; private ProductCondition condition; private BigDecimal price; private String currency;
        private int quantity; private int reservedQuantity; private String brand; private String model; private String storage;
        private String memory; private String processor; private String screenSize; private String color; private String size;
        private ProductStatus status; private Instant publishedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setSellerId(UUID value) { sellerId = value; return this; }
        public Builder setTitle(String value) { title = value; return this; }
        public Builder setDescription(String value) { description = value; return this; }
        public Builder setCategory(ProductCategory value) { category = value; return this; }
        public Builder setCondition(ProductCondition value) { condition = value; return this; }
        public Builder setPrice(BigDecimal value) { price = value; return this; }
        public Builder setCurrency(String value) { currency = value; return this; }
        public Builder setQuantity(int value) { quantity = value; return this; }
        public Builder setReservedQuantity(int value) { reservedQuantity = value; return this; }
        public Builder setBrand(String value) { brand = value; return this; }
        public Builder setModel(String value) { model = value; return this; }
        public Builder setStorage(String value) { storage = value; return this; }
        public Builder setMemory(String value) { memory = value; return this; }
        public Builder setProcessor(String value) { processor = value; return this; }
        public Builder setScreenSize(String value) { screenSize = value; return this; }
        public Builder setColor(String value) { color = value; return this; }
        public Builder setSize(String value) { size = value; return this; }
        public Builder setStatus(ProductStatus value) { status = value; return this; }
        public Builder setPublishedAt(Instant value) { publishedAt = value; return this; }
        public Builder copy(Product product) {
            return setId(product.getId()).setSellerId(product.getSellerId()).setTitle(product.getTitle())
                    .setDescription(product.getDescription()).setCategory(product.getCategory()).setCondition(product.getCondition())
                    .setPrice(product.getPrice()).setCurrency(product.getCurrency()).setQuantity(product.getQuantity())
                    .setReservedQuantity(product.getReservedQuantity()).setBrand(product.getBrand()).setModel(product.getModel())
                    .setStorage(product.getStorage()).setMemory(product.getMemory()).setProcessor(product.getProcessor())
                    .setScreenSize(product.getScreenSize()).setColor(product.getColor()).setSize(product.getSize())
                    .setStatus(product.getStatus()).setPublishedAt(product.getPublishedAt());
        }
        public Product build() { return new Product(this); }
    }
}
