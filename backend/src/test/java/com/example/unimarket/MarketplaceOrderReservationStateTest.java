package com.example.unimarket;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.enums.FulfillmentMethod;
import com.example.unimarket.domain.enums.OrderStatus;

class MarketplaceOrderReservationStateTest {
    @Test
    void reservationFlagTracksCheckoutHoldThroughCancellation() {
        MarketplaceOrder order = new MarketplaceOrder.Builder().setId(UUID.randomUUID()).setBuyerId(UUID.randomUUID())
                .setReference("UM-TEST").setTotalAmount(BigDecimal.ONE).setCurrency("ZAR").setItemCount(1)
                .setStatus(OrderStatus.PENDING_PAYMENT).setFulfillmentMethod(FulfillmentMethod.CAMPUS_PICKUP)
                .setPlacedAt(Instant.parse("2026-01-01T10:00:00Z")).build();

        assertTrue(order.activateReservation(Instant.parse("2026-01-01T10:15:00Z")));
        assertTrue(order.cancel());
        assertTrue(order.deactivateReservation());
        assertFalse(order.isReservationActive());
    }
}
