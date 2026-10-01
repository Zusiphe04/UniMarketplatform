package com.example.unimarket.response;

import com.example.unimarket.domain.ProductImage;
import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String imageUrl,
        String altText,
        int displayOrder,
        boolean primary
) {
    public static ProductImageResponse from(ProductImage value) {
        return new ProductImageResponse(value.getId(), value.getImageUrl(), value.getAltText(),
                value.getDisplayOrder(), value.isPrimaryImage());
    }
}
