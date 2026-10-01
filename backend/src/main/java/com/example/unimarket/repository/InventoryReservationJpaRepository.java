package com.example.unimarket.repository;

import com.example.unimarket.domain.InventoryReservation;
import com.example.unimarket.domain.enums.InventoryReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from InventoryReservation r where r.orderId = :orderId order by r.productId asc")
    List<InventoryReservation> findByOrderIdForUpdate(@Param("orderId") UUID orderId);

    List<InventoryReservation> findByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAscIdAsc(
            InventoryReservationStatus status, Instant now, Pageable pageable);
}
