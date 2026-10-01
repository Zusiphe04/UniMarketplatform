package com.example.unimarket.factory;

import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.util.Helper;

import java.math.BigDecimal;
import java.util.UUID;

public final class OrderItemFactory {
    private OrderItemFactory() { }
    public static OrderItem create(UUID orderId, Product product, int quantity) {
        if (orderId == null || product == null || quantity < 1) return null;
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2);
        return new OrderItem.Builder().setId(Helper.generateId()).setOrderId(orderId)
                .setProductId(product.getId()).setSellerId(product.getSellerId())
                .setProductTitle(product.getTitle()).setQuantity(quantity)
                .setUnitPrice(product.getPrice()).setSubtotal(subtotal)
                .setCurrency(product.getCurrency()).build();
    }
}
