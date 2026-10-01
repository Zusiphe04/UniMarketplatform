package com.example.unimarket.util;

import com.example.unimarket.request.CheckoutRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Produces a stable fingerprint of the checkout fields governed by idempotency. */
public final class CheckoutRequestFingerprint {
    private CheckoutRequestFingerprint() { }

    /**
     * Encodes explicit field lengths so null, empty, and embedded delimiters cannot
     * collide. The raw delivery address is used: a replay must match the submitted
     * fulfillment/delivery payload, not merely the normalized order address.
     */
    public static String from(CheckoutRequest request) {
        if (request == null || request.fulfillmentMethod() == null) {
            return null;
        }
        String deliveryAddress = request.deliveryAddress();
        String payload = request.fulfillmentMethod().name().length() + ":"
                + request.fulfillmentMethod().name() + ":"
                + (deliveryAddress == null ? -1 : deliveryAddress.length()) + ":"
                + (deliveryAddress == null ? "" : deliveryAddress);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }
}
