package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedPayment;
import java.util.List;
import java.util.UUID;

public interface ISimulatedPaymentRepository extends IRepository<SimulatedPayment, UUID> {
    SimulatedPayment readByBuyerIdAndIdempotencyKey(UUID buyerId, String key);
    List<SimulatedPayment> readByBuyerId(UUID buyerId);
}
