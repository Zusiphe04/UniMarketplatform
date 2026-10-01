package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class OrderItemRepositoryImpl implements IOrderItemRepository {
    private final OrderItemJpaRepository jpaRepository;
    public OrderItemRepositoryImpl(OrderItemJpaRepository repository) { jpaRepository = repository; }
    @Override public OrderItem create(OrderItem value) { return value == null ? null : jpaRepository.save(value); }
    @Override public OrderItem read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public OrderItem update(OrderItem value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<OrderItem> getAll() { return jpaRepository.findAll(); }
    @Override public List<OrderItem> readByOrderId(UUID id) {
        return id == null ? List.of() : jpaRepository.findByOrderIdOrderByCreatedAtAsc(id);
    }
    @Override public List<OrderItem> readByOrderIds(Collection<UUID> ids) {
        return ids == null || ids.isEmpty() ? List.of() : jpaRepository.findByOrderIdInOrderByOrderIdAscCreatedAtAsc(ids);
    }
    @Override public List<OrderItem> readBySellerId(UUID id) {
        return id == null ? List.of() : jpaRepository.findBySellerIdOrderByCreatedAtDesc(id);
    }
    @Override public boolean existsByOrderIdAndSellerId(UUID orderId, UUID sellerId) {
        return orderId != null && sellerId != null && jpaRepository.existsByOrderIdAndSellerId(orderId, sellerId);
    }
    @Override public boolean existsPaidPurchase(UUID buyerId, UUID productId) {
        return buyerId != null && productId != null
                && jpaRepository.existsPaidPurchase(buyerId, productId,
                List.of(OrderStatus.PAID, OrderStatus.RELEASED));
    }
}
