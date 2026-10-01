package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItemFulfillment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderItemFulfillmentRepositoryImpl implements IOrderItemFulfillmentRepository {
    private final OrderItemFulfillmentJpaRepository jpaRepository;
    public OrderItemFulfillmentRepositoryImpl(OrderItemFulfillmentJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    @Override public OrderItemFulfillment create(OrderItemFulfillment value) { return value == null ? null : jpaRepository.save(value); }
    @Override public OrderItemFulfillment read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public OrderItemFulfillment update(OrderItemFulfillment value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId()) ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) { if (id == null || !jpaRepository.existsById(id)) return false; jpaRepository.deleteById(id); return true; }
    @Override public List<OrderItemFulfillment> getAll() { return jpaRepository.findAll(); }
    @Override public OrderItemFulfillment readByOrderItemId(UUID orderItemId) {
        return orderItemId == null ? null : jpaRepository.findByOrderItemId(orderItemId).orElse(null);
    }
    @Override public OrderItemFulfillment readByOrderItemIdForUpdate(UUID orderItemId) {
        return orderItemId == null ? null : jpaRepository.findByOrderItemIdForUpdate(orderItemId).orElse(null);
    }
    @Override public List<OrderItemFulfillment> readByOrderId(UUID orderId) {
        return orderId == null ? List.of() : jpaRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
