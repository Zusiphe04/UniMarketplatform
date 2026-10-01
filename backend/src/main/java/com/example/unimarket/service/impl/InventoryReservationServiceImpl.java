package com.example.unimarket.service.impl;

import com.example.unimarket.domain.InventoryReservation;
import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.InventoryReservationStatus;
import com.example.unimarket.domain.enums.ReservationConsumptionResult;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.InventoryReservationFactory;
import com.example.unimarket.factory.ProductFactory;
import com.example.unimarket.repository.IInventoryReservationRepository;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.service.IInventoryReservationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryReservationServiceImpl implements IInventoryReservationService {
    private final IInventoryReservationRepository reservationRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final Duration reservationTtl;

    public InventoryReservationServiceImpl(IInventoryReservationRepository reservationRepository,
                                           IMarketplaceOrderRepository orderRepository,
                                           IProductRepository productRepository,
                                           @Value("${unimarket.inventory-reservation.ttl:PT15M}") Duration reservationTtl) {
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        if (reservationTtl == null || reservationTtl.isZero() || reservationTtl.isNegative()) {
            throw new IllegalArgumentException("Inventory reservation TTL must be positive.");
        }
        this.reservationTtl = reservationTtl;
    }

    @Override
    @Transactional
    public Instant reserve(MarketplaceOrder order, List<OrderItem> items) {
        if (order == null || order.getId() == null || items == null || items.isEmpty()) {
            throw new ValidationException("Inventory cannot be reserved for an empty order.");
        }
        Instant expiresAt = Instant.now().plus(reservationTtl);
        for (OrderItem item : sorted(items)) {
            Product product = productRepository.readByIdForUpdate(item.getProductId());
            if (ProductFactory.reserveStock(product, item.getQuantity()) == null) {
                throw new ValidationException("Insufficient stock for " + item.getProductTitle() + ".");
            }
            InventoryReservation reservation = InventoryReservationFactory.create(
                    order.getId(), item.getProductId(), item.getQuantity(), expiresAt);
            if (reservation == null) throw new ValidationException("The inventory reservation is invalid.");
            productRepository.update(product);
            reservationRepository.create(reservation);
        }
        if (!order.activateReservation(expiresAt)) {
            throw new ValidationException("The order cannot hold inventory in its current state.");
        }
        orderRepository.update(order);
        return expiresAt;
    }

    @Override
    @Transactional
    public ReservationConsumptionResult consumeForPayment(MarketplaceOrder order) {
        if (order == null) throw new ValidationException("The order is required.");
        List<InventoryReservation> reservations = reservationRepository.readByOrderIdForUpdate(order.getId());
        // Only pre-reservation legacy orders may use payment-time allocation.
        // A newly created order whose hold was released/expired must be checked out
        // again to acquire a fresh, authoritative reservation.
        if (!order.isReservationActive()) {
            if (reservations.isEmpty()) return ReservationConsumptionResult.LEGACY_ORDER;
            throw new ValidationException("The inventory reservation is no longer active. Please check out again before retrying payment.");
        }
        List<InventoryReservation> active = reservations.stream().filter(InventoryReservation::isActive).toList();
        if (active.isEmpty() || active.size() != reservations.size()) {
            throw new ValidationException("The order inventory reservation is no longer valid.");
        }
        Instant now = Instant.now();
        if (active.stream().anyMatch(reservation -> !reservation.getExpiresAt().isAfter(now))) {
            release(order, active, InventoryReservationStatus.EXPIRED);
            if (!order.cancel()) throw new ValidationException("The expired order cannot be cancelled.");
            orderRepository.update(order);
            return ReservationConsumptionResult.EXPIRED;
        }
        for (InventoryReservation reservation : sortedReservations(active)) {
            Product product = productRepository.readByIdForUpdate(reservation.getProductId());
            if (ProductFactory.consumeReservedStock(product, reservation.getQuantity()) == null
                    || !reservation.consume()) {
                throw new ValidationException("The order inventory reservation cannot be consumed.");
            }
            productRepository.update(product);
            reservationRepository.update(reservation);
        }
        if (!order.deactivateReservation()) {
            throw new ValidationException("The order inventory reservation cannot be completed.");
        }
        return ReservationConsumptionResult.CONSUMED;
    }

    @Override
    @Transactional
    public void releaseForCancellation(MarketplaceOrder order) {
        releaseActiveReservations(order, InventoryReservationStatus.RELEASED);
    }

    private void releaseActiveReservations(MarketplaceOrder order, InventoryReservationStatus targetStatus) {
        if (order == null || !order.isReservationActive()) return;
        List<InventoryReservation> active = reservationRepository.readByOrderIdForUpdate(order.getId()).stream()
                .filter(InventoryReservation::isActive).toList();
        release(order, active, targetStatus);
    }

    @Override
    @Transactional
    public int expireDueReservations(int maxOrders) {
        int limit = Math.max(1, Math.min(maxOrders, 100));
        List<UUID> orderIds = reservationRepository.readDueActive(limit, Instant.now()).stream()
                .map(InventoryReservation::getOrderId).distinct().toList();
        int expired = 0;
        for (UUID orderId : orderIds) {
            if (expireOrderIfDue(orderId, Instant.now())) expired++;
        }
        return expired;
    }

    private boolean expireOrderIfDue(UUID orderId, Instant now) {
        MarketplaceOrder order = orderRepository.readByIdForUpdate(orderId);
        if (order == null || !order.isReservationActive()) return false;
        List<InventoryReservation> active = reservationRepository.readByOrderIdForUpdate(orderId).stream()
                .filter(InventoryReservation::isActive).toList();
        if (active.isEmpty() || active.stream().noneMatch(reservation -> !reservation.getExpiresAt().isAfter(now))) {
            return false;
        }
        release(order, active, InventoryReservationStatus.EXPIRED);
        if (!order.cancel()) throw new ValidationException("The expired order cannot be cancelled.");
        orderRepository.update(order);
        return true;
    }

    private void release(MarketplaceOrder order, List<InventoryReservation> active,
                         InventoryReservationStatus targetStatus) {
        for (InventoryReservation reservation : sortedReservations(active)) {
            Product product = productRepository.readByIdForUpdate(reservation.getProductId());
            if (ProductFactory.releaseReservedStock(product, reservation.getQuantity()) == null
                    || !transition(reservation, targetStatus)) {
                throw new ValidationException("The order inventory reservation cannot be released.");
            }
            productRepository.update(product);
            reservationRepository.update(reservation);
        }
        if (!active.isEmpty() && !order.deactivateReservation()) {
            throw new ValidationException("The order inventory reservation cannot be released.");
        }
    }

    private boolean transition(InventoryReservation reservation, InventoryReservationStatus targetStatus) {
        return targetStatus == InventoryReservationStatus.EXPIRED ? reservation.expire() : reservation.release();
    }

    private List<OrderItem> sorted(List<OrderItem> items) {
        return items.stream().sorted(Comparator.comparing(OrderItem::getProductId)).toList();
    }

    private List<InventoryReservation> sortedReservations(List<InventoryReservation> reservations) {
        return reservations.stream().sorted(Comparator.comparing(InventoryReservation::getProductId)).toList();
    }
}
