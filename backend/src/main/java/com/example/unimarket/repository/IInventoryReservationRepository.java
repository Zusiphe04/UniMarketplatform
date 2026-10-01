package com.example.unimarket.repository;

import com.example.unimarket.domain.InventoryReservation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IInventoryReservationRepository extends IRepository<InventoryReservation, UUID> {
    List<InventoryReservation> readByOrderIdForUpdate(UUID orderId);
    List<InventoryReservation> readDueActive(int limit, Instant now);
}
