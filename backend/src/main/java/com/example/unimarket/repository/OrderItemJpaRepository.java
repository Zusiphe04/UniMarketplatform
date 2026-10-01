package com.example.unimarket.repository;

import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
    List<OrderItem> findByOrderIdInOrderByOrderIdAscCreatedAtAsc(Collection<UUID> orderIds);
    List<OrderItem> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
    boolean existsByOrderIdAndSellerId(UUID orderId, UUID sellerId);
    @Query("""
            select (count(i) > 0) from OrderItem i, MarketplaceOrder o
             where i.orderId = o.id and o.buyerId = :buyerId and i.productId = :productId
               and o.status in :statuses
            """)
    boolean existsPaidPurchase(@Param("buyerId") UUID buyerId, @Param("productId") UUID productId,
                               @Param("statuses") Collection<OrderStatus> statuses);
}
