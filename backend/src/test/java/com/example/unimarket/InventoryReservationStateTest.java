package com.example.unimarket;

import com.example.unimarket.domain.InventoryReservation;
import com.example.unimarket.domain.enums.InventoryReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryReservationStateTest {
    @Test
    void activeReservationCanBeConsumedOnlyOnce() {
        InventoryReservation reservation = activeReservation();
        assertTrue(reservation.consume());
        assertEquals(InventoryReservationStatus.CONSUMED, reservation.getStatus());
        assertFalse(reservation.release());
        assertFalse(reservation.expire());
    }

    @Test
    void activeReservationCanBeExpiredOnlyOnce() {
        InventoryReservation reservation = activeReservation();
        assertTrue(reservation.expire());
        assertEquals(InventoryReservationStatus.EXPIRED, reservation.getStatus());
        assertFalse(reservation.consume());
    }

    private InventoryReservation activeReservation() {
        return new InventoryReservation.Builder().setId(UUID.randomUUID()).setOrderId(UUID.randomUUID())
                .setProductId(UUID.randomUUID()).setQuantity(1).setStatus(InventoryReservationStatus.ACTIVE)
                .setExpiresAt(Instant.parse("2026-01-01T10:00:00Z")).build();
    }
}
