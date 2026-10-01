package com.example.unimarket.service.impl;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.OrderItemFulfillment;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.OrderItemFulfillmentFactory;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IOrderItemFulfillmentRepository;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.request.UpdateOrderItemFulfillmentRequest;
import com.example.unimarket.response.OrderItemFulfillmentResponse;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IOrderItemFulfillmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderItemFulfillmentServiceImpl implements IOrderItemFulfillmentService {
    private final IOrderItemFulfillmentRepository fulfillmentRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final INotificationService notificationService;

    public OrderItemFulfillmentServiceImpl(IOrderItemFulfillmentRepository fulfillmentRepository,
                                           IMarketplaceOrderRepository orderRepository,
                                           IOrderItemRepository orderItemRepository,
                                           INotificationService notificationService) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void initializeForPaidOrder(MarketplaceOrder order, List<OrderItem> items) {
        if (order == null || !financiallyPaid(order) || items == null) return;
        for (OrderItem item : items) {
            if (fulfillmentRepository.readByOrderItemId(item.getId()) == null) {
                fulfillmentRepository.create(OrderItemFulfillmentFactory.create(item));
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemFulfillmentResponse> listForBuyer(UUID buyerId, UUID orderId) {
        MarketplaceOrder order = ownedOrder(buyerId, orderId);
        return fulfillmentRepository.readByOrderId(order.getId()).stream().map(OrderItemFulfillmentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemFulfillmentResponse> listForSeller(UUID sellerId, UUID orderId) {
        if (!orderItemRepository.existsByOrderIdAndSellerId(orderId, sellerId)) throw ResourceNotFoundException.of("Order");
        return fulfillmentRepository.readByOrderId(orderId).stream()
                .filter(value -> sellerId.equals(value.getSellerId())).map(OrderItemFulfillmentResponse::from).toList();
    }

    @Override
    @Transactional
    public OrderItemFulfillmentResponse updateBySeller(UUID sellerId, UUID orderId, UUID orderItemId,
                                                        UpdateOrderItemFulfillmentRequest request) {
        MarketplaceOrder order = orderRepository.read(orderId);
        if (order == null || !financiallyPaid(order)) throw new ValidationException("Only paid orders can be fulfilled.");
        OrderItemFulfillment fulfillment = requiredSellerFulfillmentForUpdate(sellerId, orderId, orderItemId);
        if (!fulfillment.updateBySeller(request.status(), clean(request.pickupInstructions()), clean(request.trackingReference()), Instant.now())) {
            throw new ValidationException("That seller fulfillment transition is not allowed.");
        }
        OrderItemFulfillment saved = fulfillmentRepository.update(fulfillment);
        notificationService.send(order.getBuyerId(), NotificationType.FULFILMENT_UPDATED,
                "Order fulfillment updated", "A seller updated fulfillment for an item in order " + order.getReference() + ".",
                "ORDER", order.getId(), "fulfillment:" + saved.getId() + ":" + saved.getStatus());
        return OrderItemFulfillmentResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderItemFulfillmentResponse confirmReceipt(UUID buyerId, UUID orderId, UUID orderItemId) {
        MarketplaceOrder order = ownedOrder(buyerId, orderId);
        OrderItemFulfillment fulfillment = fulfillmentRepository.readByOrderItemIdForUpdate(orderItemId);
        if (fulfillment == null || !fulfillment.getOrderId().equals(order.getId())) throw ResourceNotFoundException.of("Order item fulfillment");
        if (!fulfillment.confirmReceived(Instant.now())) throw new ValidationException("This item is not ready to be confirmed as received.");
        OrderItemFulfillment saved = fulfillmentRepository.update(fulfillment);
        notificationService.send(saved.getSellerId(), NotificationType.FULFILMENT_UPDATED,
                "Buyer confirmed receipt", "The buyer confirmed receipt of an item in order " + order.getReference() + ".",
                "ORDER", order.getId(), "fulfillment:" + saved.getId() + ":received");
        return OrderItemFulfillmentResponse.from(saved);
    }

    private MarketplaceOrder ownedOrder(UUID buyerId, UUID orderId) {
        MarketplaceOrder order = orderRepository.read(orderId);
        if (order == null || !buyerId.equals(order.getBuyerId())) throw ResourceNotFoundException.of("Order");
        return order;
    }

    private OrderItemFulfillment requiredSellerFulfillmentForUpdate(UUID sellerId, UUID orderId, UUID orderItemId) {
        OrderItemFulfillment fulfillment = fulfillmentRepository.readByOrderItemIdForUpdate(orderItemId);
        if (fulfillment == null || !orderId.equals(fulfillment.getOrderId()) || !sellerId.equals(fulfillment.getSellerId())) {
            throw ResourceNotFoundException.of("Order item fulfillment");
        }
        return fulfillment;
    }

    private boolean financiallyPaid(MarketplaceOrder order) {
        return order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.HELD || order.getStatus() == OrderStatus.RELEASED;
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
