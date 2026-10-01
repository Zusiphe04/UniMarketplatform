package com.example.unimarket.response;

import com.example.unimarket.domain.CartItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        UUID sellerId,
        String title,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String currency,
        ProductStatus productStatus,
        int availableQuantity,
        String primaryImageUrl
) {
    public static CartItemResponse from(CartItem item, Product product, List<ProductImage> images) {
        String image = images == null ? null : images.stream().filter(ProductImage::isPrimaryImage)
                .findFirst().or(() -> images.stream().findFirst()).map(ProductImage::getImageUrl).orElse(null);
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2);
        return new CartItemResponse(item.getId(), product.getId(), product.getSellerId(), product.getTitle(),
                item.getQuantity(), product.getPrice(), subtotal, product.getCurrency(), product.getStatus(),
                product.getAvailableQuantity(), image);
    }
}
