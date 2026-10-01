package com.example.unimarket.service;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.enums.ReservationConsumptionResult;

import java.time.Instant;
import java.util.List;

public interface IInventoryReservationService {
    Instant reserve(MarketplaceOrder order, List<OrderItem> items);
    ReservationConsumptionResult consumeForPayment(MarketplaceOrder order);
    void releaseForCancellation(MarketplaceOrder order);
    int expireDueReservations(int maxOrders);
}
