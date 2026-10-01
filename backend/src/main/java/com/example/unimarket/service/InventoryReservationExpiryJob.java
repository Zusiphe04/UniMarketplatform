package com.example.unimarket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Bounded best-effort expiry sweep; request flows also expire holds lazily. */
@Component
@ConditionalOnProperty(name = "unimarket.inventory-reservation.expiry-enabled", havingValue = "true", matchIfMissing = true)
public class InventoryReservationExpiryJob {
    private final IInventoryReservationService reservationService;
    private final int batchSize;

    public InventoryReservationExpiryJob(IInventoryReservationService reservationService,
                                         @Value("${unimarket.inventory-reservation.expiry-batch-size:25}") int batchSize) {
        this.reservationService = reservationService;
        this.batchSize = Math.max(1, Math.min(batchSize, 100));
    }

    @Scheduled(fixedDelayString = "${unimarket.inventory-reservation.expiry-sweep-delay:PT1M}",
            initialDelayString = "${unimarket.inventory-reservation.expiry-initial-delay:PT1M}")
    public void releaseExpiredReservations() {
        reservationService.expireDueReservations(batchSize);
    }
}
