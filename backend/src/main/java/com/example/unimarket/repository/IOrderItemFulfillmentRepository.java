package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItemFulfillment;

import java.util.List;
import java.util.UUID;

public interface IOrderItemFulfillmentRepository extends IRepository<OrderItemFulfillment, UUID> {
    OrderItemFulfillment readByOrderItemId(UUID orderItemId);
    OrderItemFulfillment readByOrderItemIdForUpdate(UUID orderItemId);
    List<OrderItemFulfillment> readByOrderId(UUID orderId);
}
