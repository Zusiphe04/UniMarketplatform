package com.example.unimarket.repository;

import com.example.unimarket.domain.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByBuyerIdOrderByAddedAtAsc(UUID buyerId);

    /** Locks the checkout snapshot before products are inspected or cart rows are removed. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartItem c where c.buyerId = :buyerId order by c.addedAt asc")
    List<CartItem> findByBuyerIdOrderByAddedAtAscForUpdate(@Param("buyerId") UUID buyerId);

    Optional<CartItem> findByBuyerIdAndProductId(UUID buyerId, UUID productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartItem c where c.id in :ids")
    int deleteAllByIdIn(@Param("ids") Collection<UUID> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartItem c where c.buyerId = :buyerId")
    int deleteAllByBuyerId(@Param("buyerId") UUID buyerId);
}
