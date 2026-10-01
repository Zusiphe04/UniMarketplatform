package com.example.unimarket.domain.enums;

/** Result of consuming checkout inventory when a payment succeeds. */
public enum ReservationConsumptionResult {
    CONSUMED,
    EXPIRED,
    LEGACY_ORDER
}
