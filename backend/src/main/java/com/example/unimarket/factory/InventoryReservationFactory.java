package com.example.unimarket.factory;

import com.example.unimarket.domain.InventoryReservation;
import com.example.unimarket.domain.enums.InventoryReservationStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

/** Creates valid inventory holds for awaiting-payment orders. */
public final class InventoryReservationFactory {
    private InventoryReservationFactory() { }

    public static InventoryReservation create(UUID orderId, UUID productId, int quantity, Instant expiresAt) {
        if (orderId == null || productId == null || quantity < 1 || expiresAt == null) return null;
        return new InventoryReservation.Builder().setId(Helper.generateId()).setOrderId(orderId)
                .setProductId(productId).setQuantity(quantity).setStatus(InventoryReservationStatus.ACTIVE)
                .setExpiresAt(expiresAt).build();
    }
}
