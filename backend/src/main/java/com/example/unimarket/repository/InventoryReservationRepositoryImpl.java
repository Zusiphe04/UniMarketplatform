package com.example.unimarket.repository;

import com.example.unimarket.domain.InventoryReservation;
import com.example.unimarket.domain.enums.InventoryReservationStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class InventoryReservationRepositoryImpl implements IInventoryReservationRepository {
    private final InventoryReservationJpaRepository jpaRepository;
    public InventoryReservationRepositoryImpl(InventoryReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public InventoryReservation create(InventoryReservation value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public InventoryReservation read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public InventoryReservation update(InventoryReservation value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<InventoryReservation> getAll() { return jpaRepository.findAll(); }
    @Override public List<InventoryReservation> readByOrderIdForUpdate(UUID orderId) {
        return orderId == null ? List.of() : jpaRepository.findByOrderIdForUpdate(orderId);
    }
    @Override public List<InventoryReservation> readDueActive(int limit, Instant now) {
        return limit < 1 || now == null ? List.of()
                : jpaRepository.findByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAscIdAsc(
                        InventoryReservationStatus.ACTIVE, now, PageRequest.of(0, limit));
    }
}
