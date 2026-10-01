package com.example.unimarket.repository;

import com.example.unimarket.domain.MarketplaceOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketplaceOrderJpaRepository extends JpaRepository<MarketplaceOrder, UUID> {
    List<MarketplaceOrder> findByBuyerIdOrderByPlacedAtDesc(UUID buyerId);
    Optional<MarketplaceOrder> findByBuyerIdAndIdempotencyKey(UUID buyerId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from MarketplaceOrder o where o.id = :id")
    Optional<MarketplaceOrder> findByIdForUpdate(@Param("id") UUID id);
}
