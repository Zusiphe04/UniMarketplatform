package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedPayment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SimulatedPaymentRepositoryImpl implements ISimulatedPaymentRepository {
    private final SimulatedPaymentJpaRepository jpaRepository;
    public SimulatedPaymentRepositoryImpl(SimulatedPaymentJpaRepository repository) { jpaRepository = repository; }
    @Override public SimulatedPayment create(SimulatedPayment value) { return value == null ? null : jpaRepository.save(value); }
    @Override public SimulatedPayment read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public SimulatedPayment update(SimulatedPayment value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<SimulatedPayment> getAll() { return jpaRepository.findAll(); }
    @Override public SimulatedPayment readByBuyerIdAndIdempotencyKey(UUID buyerId, String key) {
        return buyerId == null || key == null ? null
                : jpaRepository.findByBuyerIdAndIdempotencyKey(buyerId, key.trim()).orElse(null);
    }
    @Override public List<SimulatedPayment> readByBuyerId(UUID id) {
        return id == null ? List.of() : jpaRepository.findByBuyerIdOrderByProcessedAtDesc(id);
    }
}
