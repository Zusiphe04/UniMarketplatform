package com.example.unimarket.repository;

import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SimulatedEscrowRepositoryImpl implements ISimulatedEscrowRepository {
    private final SimulatedEscrowJpaRepository jpaRepository;

    public SimulatedEscrowRepositoryImpl(SimulatedEscrowJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public SimulatedEscrow create(SimulatedEscrow value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public SimulatedEscrow read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public SimulatedEscrow update(SimulatedEscrow value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<SimulatedEscrow> getAll() { return jpaRepository.findAll(); }
    @Override public SimulatedEscrow readByOrderId(UUID orderId) {
        return orderId == null ? null : jpaRepository.findByOrderId(orderId).orElse(null);
    }
    @Override public Page<SimulatedEscrow> readByStatus(EscrowStatus status, Pageable pageable) {
        return jpaRepository.findByStatusOrderByDisputedAtAsc(status, pageable);
    }
}
