package com.example.unimarket;

import com.example.unimarket.domain.OrderItemFulfillment;
import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderItemFulfillmentStateTest {
    @Test
    void sellerAndBuyerTransitionsAreConstrained() {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        OrderItemFulfillment fulfillment = new OrderItemFulfillment.Builder()
                .setId(UUID.randomUUID()).setOrderItemId(UUID.randomUUID()).setOrderId(UUID.randomUUID())
                .setSellerId(UUID.randomUUID()).setStatus(OrderItemFulfillmentStatus.AWAITING_SELLER).build();

        assertFalse(fulfillment.confirmReceived(now));
        assertTrue(fulfillment.updateBySeller(OrderItemFulfillmentStatus.ACCEPTED, null, null, now));
        assertTrue(fulfillment.updateBySeller(OrderItemFulfillmentStatus.DISPATCHED, null, "TRACK-100", now.plusSeconds(10)));
        assertTrue(fulfillment.confirmReceived(now.plusSeconds(20)));
        assertEquals(OrderItemFulfillmentStatus.RECEIVED, fulfillment.getStatus());
    }
}
