package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ISimulatedEscrowRepository extends IRepository<SimulatedEscrow, UUID> {
    SimulatedEscrow readByOrderId(UUID orderId);
    Page<SimulatedEscrow> readByStatus(EscrowStatus status, Pageable pageable);
}
