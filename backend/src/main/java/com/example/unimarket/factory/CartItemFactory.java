package com.example.unimarket.factory;

import com.example.unimarket.domain.CartItem;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class CartItemFactory {
    private CartItemFactory() { }
    public static CartItem create(UUID buyerId, UUID productId, int quantity) {
        if (buyerId == null || productId == null || quantity < 1 || quantity > 99) return null;
        return new CartItem.Builder().setId(Helper.generateId()).setBuyerId(buyerId)
                .setProductId(productId).setQuantity(quantity).setAddedAt(Instant.now()).build();
    }
    public static CartItem changeQuantity(CartItem value, int quantity) {
        return value != null && value.changeQuantity(quantity) ? value : null;
    }
}
