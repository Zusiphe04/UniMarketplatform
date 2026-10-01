package com.example.unimarket.domain.enums;

/** Operational state of a single seller-owned line item after payment. */
public enum OrderItemFulfillmentStatus {
    AWAITING_SELLER,
    ACCEPTED,
    READY_FOR_COLLECTION,
    DISPATCHED,
    RECEIVED
}
