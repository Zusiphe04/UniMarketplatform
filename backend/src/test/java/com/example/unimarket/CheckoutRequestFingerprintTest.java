package com.example.unimarket;

import com.example.unimarket.domain.enums.FulfillmentMethod;
import com.example.unimarket.request.CheckoutRequest;
import com.example.unimarket.util.CheckoutRequestFingerprint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CheckoutRequestFingerprintTest {
    @Test
    void fingerprintsOnlyReplayAnExactlyMatchingFulfillmentAndDeliveryPayload() {
        CheckoutRequest original = new CheckoutRequest(FulfillmentMethod.LOCAL_DELIVERY, "12 Campus Road");
        assertEquals(CheckoutRequestFingerprint.from(original), CheckoutRequestFingerprint.from(
                new CheckoutRequest(FulfillmentMethod.LOCAL_DELIVERY, "12 Campus Road")));
        assertNotEquals(CheckoutRequestFingerprint.from(original), CheckoutRequestFingerprint.from(
                new CheckoutRequest(FulfillmentMethod.CAMPUS_PICKUP, "12 Campus Road")));
        assertNotEquals(CheckoutRequestFingerprint.from(original), CheckoutRequestFingerprint.from(
                new CheckoutRequest(FulfillmentMethod.LOCAL_DELIVERY, " 12 Campus Road")));
    }
}
