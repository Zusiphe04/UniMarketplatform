package com.example.unimarket.service.impl;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowResolution;
import com.example.unimarket.domain.enums.EscrowStatus;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.MarketplaceOrderFactory;
import com.example.unimarket.factory.SimulatedEscrowFactory;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.repository.ISimulatedEscrowRepository;
import com.example.unimarket.request.OpenEscrowDisputeRequest;
import com.example.unimarket.request.ResolveEscrowRequest;
import com.example.unimarket.response.EscrowResponse;
import com.example.unimarket.service.IEngagementService;
import com.example.unimarket.service.IEscrowService;
import com.example.unimarket.service.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EscrowServiceImpl implements IEscrowService {
    private final ISimulatedEscrowRepository escrowRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final INotificationService notificationService;
    private final IEngagementService engagementService;

    public EscrowServiceImpl(ISimulatedEscrowRepository escrowRepository,
                             IMarketplaceOrderRepository orderRepository,
                             IOrderItemRepository orderItemRepository,
                             INotificationService notificationService,
                             IEngagementService engagementService) {
        this.escrowRepository = escrowRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
        this.engagementService = engagementService;
    }

    @Override
    @Transactional(readOnly = true)
    public EscrowResponse getBuyerEscrow(UUID buyerId, UUID orderId) {
        MarketplaceOrder order = ownedOrder(buyerId, orderId, false);
        SimulatedEscrow escrow = escrowRepository.readByOrderId(orderId);
        if (escrow == null) throw ResourceNotFoundException.of("Escrow");
        return EscrowResponse.from(escrow, order);
    }

    @Override
    @Transactional
    public EscrowResponse confirmReceipt(UUID buyerId, UUID orderId) {
        MarketplaceOrder order = ownedOrder(buyerId, orderId, true);
        SimulatedEscrow escrow = requiredEscrow(orderId);
        if (order.getStatus() == OrderStatus.RELEASED && escrow.getStatus() == EscrowStatus.RELEASED) {
            return EscrowResponse.from(escrow, order);
        }
        if (MarketplaceOrderFactory.confirmReceipt(order) == null
                || SimulatedEscrowFactory.confirmReceipt(escrow, buyerId) == null) {
            throw new ValidationException("Only a held escrow can be released by buyer confirmation.");
        }
        orderRepository.update(order);
        escrowRepository.update(escrow);
        List<OrderItem> items = orderItemRepository.readByOrderId(orderId);
        engagementService.recordCompletedOrder(order, items);
        sendStatusNotifications(order, items, NotificationType.ESCROW_RELEASED,
                "Escrow released", "Buyer receipt was confirmed and the simulated escrow was released.");
        return EscrowResponse.from(escrow, order);
    }

    @Override
    @Transactional
    public EscrowResponse openDispute(UUID buyerId, UUID orderId, OpenEscrowDisputeRequest request) {
        MarketplaceOrder order = ownedOrder(buyerId, orderId, true);
        SimulatedEscrow escrow = requiredEscrow(orderId);
        if (order.getStatus() == OrderStatus.DISPUTED && escrow.getStatus() == EscrowStatus.DISPUTED) {
            return EscrowResponse.from(escrow, order);
        }
        if (MarketplaceOrderFactory.openDispute(order) == null
                || SimulatedEscrowFactory.openDispute(escrow, request.reason()) == null) {
            throw new ValidationException("Only a held escrow can be disputed.");
        }
        orderRepository.update(order);
        escrowRepository.update(escrow);
        List<OrderItem> items = orderItemRepository.readByOrderId(orderId);
        sendStatusNotifications(order, items, NotificationType.ESCROW_DISPUTED,
                "Escrow disputed", "The buyer opened a dispute and the simulated escrow remains frozen.");
        return EscrowResponse.from(escrow, order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EscrowResponse> listDisputes(int page, int size) {
        if (page < 0 || size < 1) throw new ValidationException("Invalid escrow page request.");
        return escrowRepository.readByStatus(EscrowStatus.DISPUTED,
                PageRequest.of(page, Math.min(size, 100))).map(escrow -> {
                    MarketplaceOrder order = orderRepository.read(escrow.getOrderId());
                    if (order == null) throw ResourceNotFoundException.of("Order");
                    return EscrowResponse.from(escrow, order);
                });
    }

    @Override
    @Transactional
    public EscrowResponse resolve(UUID moderatorId, UUID orderId, ResolveEscrowRequest request) {
        MarketplaceOrder order = orderRepository.readByIdForUpdate(orderId);
        if (order == null) throw ResourceNotFoundException.of("Order");
        SimulatedEscrow escrow = requiredEscrow(orderId);
        EscrowStatus requestedStatus = request.resolution() == EscrowResolution.RELEASE
                ? EscrowStatus.RELEASED : EscrowStatus.REFUNDED;
        if (escrow.getStatus() == requestedStatus && escrow.getResolution() == request.resolution()) {
            return EscrowResponse.from(escrow, order);
        }
        if (MarketplaceOrderFactory.resolveDispute(order, request.resolution()) == null
                || SimulatedEscrowFactory.resolve(escrow, moderatorId,
                request.resolution(), request.note()) == null) {
            throw new ValidationException("Only an open escrow dispute can be resolved.");
        }
        orderRepository.update(order);
        escrowRepository.update(escrow);
        List<OrderItem> items = orderItemRepository.readByOrderId(orderId);
        if (request.resolution() == EscrowResolution.RELEASE) {
            engagementService.recordCompletedOrder(order, items);
            sendStatusNotifications(order, items, NotificationType.ESCROW_RELEASED,
                    "Escrow released", "A moderator resolved the dispute by releasing the simulated escrow.");
        } else {
            sendStatusNotifications(order, items, NotificationType.ESCROW_REFUNDED,
                    "Escrow refunded", "A moderator resolved the dispute with a simulated refund. Inventory was not restocked.");
        }
        return EscrowResponse.from(escrow, order);
    }

    private MarketplaceOrder ownedOrder(UUID buyerId, UUID orderId, boolean lock) {
        MarketplaceOrder order = lock ? orderRepository.readByIdForUpdate(orderId) : orderRepository.read(orderId);
        if (order == null || buyerId == null || !buyerId.equals(order.getBuyerId())) {
            throw ResourceNotFoundException.of("Order");
        }
        return order;
    }

    private SimulatedEscrow requiredEscrow(UUID orderId) {
        SimulatedEscrow escrow = escrowRepository.readByOrderId(orderId);
        if (escrow == null) throw ResourceNotFoundException.of("Escrow");
        return escrow;
    }

    private void sendStatusNotifications(MarketplaceOrder order, List<OrderItem> items,
                                         NotificationType type, String title, String message) {
        String state = order.getStatus().name().toLowerCase();
        notificationService.send(order.getBuyerId(), type, title,
                message + " Order " + order.getReference() + ".", "ORDER", order.getId(),
                "order:" + order.getId() + ":escrow:" + state + ":buyer");
        items.stream().map(item -> item.getSellerId()).distinct().forEach(sellerId -> notificationService.send(
                sellerId, type, title, message + " Order " + order.getReference() + ".",
                "ORDER", order.getId(),
                "order:" + order.getId() + ":escrow:" + state + ":seller:" + sellerId));
    }
}
