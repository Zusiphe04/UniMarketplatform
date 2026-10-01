package com.example.unimarket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;
import java.util.UUID;

/** Ordered externally hosted product media metadata; binary content is not stored in MySQL. */
@Entity
@Table(name = "product_image",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_image_order", columnNames = {"product_id", "display_order"}),
        indexes = @Index(name = "idx_product_image_product", columnList = "product_id, display_order"))
public class ProductImage extends AuditableEntity {
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "image_url", nullable = false, length = 1000) private String imageUrl;
    @Column(name = "alt_text", length = 200) private String altText;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "primary_image", nullable = false) private boolean primaryImage;

    protected ProductImage() { super(); }
    private ProductImage(Builder builder) {
        super(builder.id);
        productId = builder.productId;
        imageUrl = builder.imageUrl;
        altText = builder.altText;
        displayOrder = builder.displayOrder;
        primaryImage = builder.primaryImage;
    }
    public UUID getProductId() { return productId; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isPrimaryImage() { return primaryImage; }
    public void setPrimaryImage(boolean value) { primaryImage = value; }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof ProductImage value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id;
        private UUID productId;
        private String imageUrl;
        private String altText;
        private int displayOrder;
        private boolean primaryImage;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setProductId(UUID value) { productId = value; return this; }
        public Builder setImageUrl(String value) { imageUrl = value; return this; }
        public Builder setAltText(String value) { altText = value; return this; }
        public Builder setDisplayOrder(int value) { displayOrder = value; return this; }
        public Builder setPrimaryImage(boolean value) { primaryImage = value; return this; }
        public Builder copy(ProductImage value) {
            return setId(value.getId()).setProductId(value.getProductId()).setImageUrl(value.getImageUrl())
                    .setAltText(value.getAltText()).setDisplayOrder(value.getDisplayOrder())
                    .setPrimaryImage(value.isPrimaryImage());
        }
        public ProductImage build() { return new ProductImage(this); }
    }
}
