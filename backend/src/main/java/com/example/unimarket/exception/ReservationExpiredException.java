package com.example.unimarket.exception;

/** Signals an expired checkout hold after its release has been committed. */
public final class ReservationExpiredException extends ValidationException {
    public ReservationExpiredException() {
        super("The payment reservation expired. The order has been cancelled and stock was released.");
    }
}
