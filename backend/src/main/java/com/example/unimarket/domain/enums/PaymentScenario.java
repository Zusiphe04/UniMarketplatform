package com.example.unimarket.domain.enums;

/** Explicit outcome selected for the safe simulated-payment flow. */
public enum PaymentScenario {
    SUCCESS,
    DECLINED,
    INSUFFICIENT_FUNDS,
    TIMEOUT
}
