package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItem;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IOrderItemRepository extends IRepository<OrderItem, UUID> {
    List<OrderItem> readByOrderId(UUID orderId);
    List<OrderItem> readByOrderIds(Collection<UUID> orderIds);
    List<OrderItem> readBySellerId(UUID sellerId);
    boolean existsByOrderIdAndSellerId(UUID orderId, UUID sellerId);
    boolean existsPaidPurchase(UUID buyerId, UUID productId);
}
