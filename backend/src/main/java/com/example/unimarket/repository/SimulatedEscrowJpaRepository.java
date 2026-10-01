package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SimulatedEscrowJpaRepository extends JpaRepository<SimulatedEscrow, UUID> {
    Optional<SimulatedEscrow> findByOrderId(UUID orderId);
    Page<SimulatedEscrow> findByStatusOrderByDisputedAtAsc(EscrowStatus status, Pageable pageable);
}
