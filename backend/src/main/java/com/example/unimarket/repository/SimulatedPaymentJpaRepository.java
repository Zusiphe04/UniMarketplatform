package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulatedPaymentJpaRepository extends JpaRepository<SimulatedPayment, UUID> {
    Optional<SimulatedPayment> findByBuyerIdAndIdempotencyKey(UUID buyerId, String key);
    List<SimulatedPayment> findByBuyerIdOrderByProcessedAtDesc(UUID buyerId);
}
