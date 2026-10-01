package com.example.unimarket.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.SimulatedPayment;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentScenario;
import com.example.unimarket.domain.enums.ReservationConsumptionResult;
import com.example.unimarket.exception.ReservationExpiredException;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.MarketplaceOrderFactory;
import com.example.unimarket.factory.ProductFactory;
import com.example.unimarket.factory.SimulatedEscrowFactory;
import com.example.unimarket.factory.SimulatedPaymentFactory;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.repository.ISimulatedEscrowRepository;
import com.example.unimarket.repository.ISimulatedPaymentRepository;
import com.example.unimarket.request.SimulatePaymentRequest;
import com.example.unimarket.response.PaymentOptionResponse;
import com.example.unimarket.response.SimulatedPaymentResponse;
import com.example.unimarket.service.IEngagementService;
import com.example.unimarket.service.IInventoryReservationService;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IOrderItemFulfillmentService;
import com.example.unimarket.service.ISimulatedPaymentService;

@Service
public class SimulatedPaymentServiceImpl implements ISimulatedPaymentService {
    private final ISimulatedPaymentRepository paymentRepository;
    private final ISimulatedEscrowRepository escrowRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IProductRepository productRepository;
    private final IInventoryReservationService reservationService;
    private final INotificationService notificationService;
    private final IEngagementService engagementService;
    private final IOrderItemFulfillmentService fulfillmentService;
    private final BigDecimal escrowThreshold;

    public SimulatedPaymentServiceImpl(ISimulatedPaymentRepository paymentRepository,
                                       ISimulatedEscrowRepository escrowRepository,
                                       IMarketplaceOrderRepository orderRepository,
                                       IOrderItemRepository orderItemRepository,
                                       IProductRepository productRepository,
                                       IInventoryReservationService reservationService,
                                       INotificationService notificationService,
                                       IEngagementService engagementService,
                                       IOrderItemFulfillmentService fulfillmentService,
                                       @Value("${unimarket.escrow.threshold-zar:5000.00}") BigDecimal escrowThreshold) {
        this.paymentRepository = paymentRepository; this.escrowRepository = escrowRepository;
        this.orderRepository = orderRepository; this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository; this.reservationService = reservationService;
        this.notificationService = notificationService; this.engagementService = engagementService;
        this.fulfillmentService = fulfillmentService;
        if (escrowThreshold == null || escrowThreshold.signum() <= 0) {
            throw new IllegalArgumentException("The simulated escrow threshold must be positive.");
        }
        this.escrowThreshold = escrowThreshold;
    }

    @Override public List<PaymentOptionResponse> options() {
        return Arrays.stream(PaymentOption.values()).map(PaymentOptionResponse::from).toList();
    }

    @Override
    @Transactional(noRollbackFor = ReservationExpiredException.class)
    public SimulatedPaymentResponse simulate(UUID buyerId, String idempotencyKey, SimulatePaymentRequest request) {
        String key = idempotencyKey == null ? null : idempotencyKey.trim();
        if (key == null || key.isBlank() || key.length() > 120) {
            throw new ValidationException("Idempotency-Key is required and must not exceed 120 characters.");
        }
        SimulatedPayment replay = paymentRepository.readByBuyerIdAndIdempotencyKey(buyerId, key);
        if (replay != null) return replayResponse(replay, request);

        MarketplaceOrder order = orderRepository.readByIdForUpdate(request.orderId());
        if (order == null || !order.getBuyerId().equals(buyerId)) throw ResourceNotFoundException.of("Order");

        // The early replay read can race another request with the same key. Re-check after
        // the order lock so the loser returns the committed result instead of a state error.
        replay = paymentRepository.readByBuyerIdAndIdempotencyKey(buyerId, key);
        if (replay != null) return replayResponse(replay, request);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new ValidationException("This order is not awaiting payment.");
        }
        List<OrderItem> items = orderItemRepository.readByOrderId(order.getId());
        if (items.isEmpty()) throw new ValidationException("The order contains no items.");

        if (request.scenario() == PaymentScenario.SUCCESS) {
            ReservationConsumptionResult consumption = reservationService.consumeForPayment(order);
            if (consumption == ReservationConsumptionResult.EXPIRED) {
                // This exception is deliberately excluded from rollback so the release/cancellation is durable.
                throw new ReservationExpiredException();
            }
            if (consumption == ReservationConsumptionResult.LEGACY_ORDER) applyLegacyStock(items);
            boolean holdInEscrow = order.getTotalAmount().compareTo(escrowThreshold) >= 0;
            MarketplaceOrder transitioned = holdInEscrow ? MarketplaceOrderFactory.markHeld(order)
                    : MarketplaceOrderFactory.markPaid(order);
            if (transitioned == null) throw new ValidationException("The order cannot record a successful payment.");
        } else {
            String code = switch (request.scenario()) {
                case DECLINED -> "DECLINED";
                case INSUFFICIENT_FUNDS -> "INSUFFICIENT_FUNDS";
                case TIMEOUT -> "TIMEOUT";
                case SUCCESS -> throw new IllegalStateException("Unexpected success scenario");
            };
            if (MarketplaceOrderFactory.markPaymentFailed(order, code) == null) {
                throw new ValidationException("The order cannot record a failed payment.");
            }
            // Keep the checkout hold until its original expiry so PAYMENT_FAILED remains
            // genuinely retryable. The expiry job releases it if no successful retry arrives.
        }
        orderRepository.update(order);
        if (order.getStatus() == OrderStatus.HELD) {
            SimulatedEscrow escrow = SimulatedEscrowFactory.createHeld(order);
            if (escrow == null) throw new ValidationException("The simulated escrow could not be created.");
            escrowRepository.create(escrow);
        }
        if (request.scenario() == PaymentScenario.SUCCESS) fulfillmentService.initializeForPaidOrder(order, items);

        SimulatedPayment payment = SimulatedPaymentFactory.create(buyerId, order, request.paymentOption(), request.scenario(), key);
        if (payment == null) throw new ValidationException("The payment simulation request is invalid.");
        SimulatedPayment saved = paymentRepository.create(payment);
        engagementService.recordCompletedOrder(order, items);
        sendPaymentNotifications(saved, order, items);
        return SimulatedPaymentResponse.from(saved, order);
    }

    private SimulatedPaymentResponse replayResponse(SimulatedPayment replay, SimulatePaymentRequest request) {
        if (!replay.getOrderId().equals(request.orderId()) || replay.getPaymentOption() != request.paymentOption()
                || replay.getScenario() != request.scenario()) {
            throw new ValidationException("This Idempotency-Key was already used with a different request.");
        }
        return SimulatedPaymentResponse.from(replay, orderRepository.read(replay.getOrderId()));
    }

    @Override @Transactional(readOnly = true)
    public List<SimulatedPaymentResponse> list(UUID buyerId) {
        return paymentRepository.readByBuyerId(buyerId).stream()
                .map(payment -> SimulatedPaymentResponse.from(payment, orderRepository.read(payment.getOrderId()))).toList();
    }
    @Override @Transactional(readOnly = true)
    public SimulatedPaymentResponse get(UUID buyerId, UUID paymentId) {
        SimulatedPayment payment = paymentRepository.read(paymentId);
        if (payment == null || !payment.getBuyerId().equals(buyerId)) throw ResourceNotFoundException.of("Payment");
        return SimulatedPaymentResponse.from(payment, orderRepository.read(payment.getOrderId()));
    }
    private void applyLegacyStock(List<OrderItem> items) {
        for (OrderItem item : items.stream().sorted(Comparator.comparing(OrderItem::getProductId)).toList()) {
            Product product = productRepository.readByIdForUpdate(item.getProductId());
            if (ProductFactory.decrementStock(product, item.getQuantity()) == null) {
                throw new ValidationException("Stock is no longer available for " + item.getProductTitle() + ".");
            }
            productRepository.update(product);
        }
    }
    private void sendPaymentNotifications(SimulatedPayment payment, MarketplaceOrder order, List<OrderItem> items) {
        boolean succeeded = payment.getScenario() == PaymentScenario.SUCCESS;
        boolean held = order.getStatus() == OrderStatus.HELD;
        String paymentName = payment.getPaymentOption().getDisplayName();
        notificationService.send(payment.getBuyerId(), succeeded ? NotificationType.PAYMENT_SUCCEEDED : NotificationType.PAYMENT_FAILED,
                succeeded ? "Payment successful" : "Payment failed", succeeded
                        ? "Payment via " + paymentName + " for order " + order.getReference() + " was successful."
                        : "Payment via " + paymentName + " for order " + order.getReference() + " failed: "
                                + payment.getScenario().name().replace('_', ' ') + ".",
                "ORDER", order.getId(), "payment:" + payment.getId() + ":buyer");
        if (!succeeded) return;
        if (held) notificationService.send(payment.getBuyerId(), NotificationType.ESCROW_HELD,
                "Payment held in simulated escrow", "Order " + order.getReference()
                        + " requires your receipt confirmation before release.", "ORDER", order.getId(),
                "order:" + order.getId() + ":escrow:held:buyer");
        items.stream().map(OrderItem::getSellerId).distinct().forEach(sellerId -> notificationService.send(sellerId,
                held ? NotificationType.ESCROW_HELD : NotificationType.SELLER_ORDER_PAID,
                held ? "Order payment held in escrow" : "Order paid", held
                        ? "Order " + order.getReference() + " was paid; simulated funds remain held until buyer confirmation."
                        : "Order " + order.getReference() + " has been paid and is ready for fulfilment.",
                "ORDER", order.getId(), held ? "order:" + order.getId() + ":escrow:held:seller:" + sellerId
                        : "payment:" + payment.getId() + ":seller:" + sellerId));
    }
}
