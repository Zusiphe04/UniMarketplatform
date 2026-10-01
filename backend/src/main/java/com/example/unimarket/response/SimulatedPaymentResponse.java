package com.example.unimarket.response;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.SimulatedPayment;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentScenario;
import com.example.unimarket.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SimulatedPaymentResponse(
        UUID id, UUID orderId, String orderReference, BigDecimal amount, String currency,
        PaymentOption paymentOption, PaymentScenario scenario, PaymentStatus status,
        OrderStatus orderStatus, OrderStatus currentOrderStatus,
        String reference, String failureCode, Instant processedAt
) {
    public static SimulatedPaymentResponse from(SimulatedPayment payment, MarketplaceOrder order) {
        OrderStatus resultingStatus = payment.getResultingOrderStatus() == null
                ? order.getStatus() : payment.getResultingOrderStatus();
        return new SimulatedPaymentResponse(payment.getId(), payment.getOrderId(), order.getReference(),
                payment.getAmount(), payment.getCurrency(), payment.getPaymentOption(), payment.getScenario(),
                payment.getStatus(), resultingStatus, order.getStatus(), payment.getReference(),
                payment.getFailureCode(), payment.getProcessedAt());
    }
}
