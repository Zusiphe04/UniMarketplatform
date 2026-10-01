package com.example.unimarket.factory;

import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.OrderItemFulfillment;
import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;
import com.example.unimarket.util.Helper;

public final class OrderItemFulfillmentFactory {
    private OrderItemFulfillmentFactory() { }
    public static OrderItemFulfillment create(OrderItem item) {
        if (item == null) return null;
        return new OrderItemFulfillment.Builder().setId(Helper.generateId()).setOrderItemId(item.getId())
                .setOrderId(item.getOrderId()).setSellerId(item.getSellerId())
                .setStatus(OrderItemFulfillmentStatus.AWAITING_SELLER).build();
    }
}
