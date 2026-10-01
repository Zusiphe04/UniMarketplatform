package com.example.unimarket.domain.enums;

/** Lifecycle state of a stock hold created during checkout. */
public enum InventoryReservationStatus {
    ACTIVE,
    CONSUMED,
    RELEASED,
    EXPIRED
}
