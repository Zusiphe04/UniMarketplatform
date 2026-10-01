package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItemFulfillment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemFulfillmentJpaRepository extends JpaRepository<OrderItemFulfillment, UUID> {
    Optional<OrderItemFulfillment> findByOrderItemId(UUID orderItemId);
    List<OrderItemFulfillment> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from OrderItemFulfillment f where f.orderItemId = :orderItemId")
    Optional<OrderItemFulfillment> findByOrderItemIdForUpdate(@Param("orderItemId") UUID orderItemId);
}
