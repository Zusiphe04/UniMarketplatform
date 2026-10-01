package com.example.unimarket.factory;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.SimulatedPayment;
import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentScenario;
import com.example.unimarket.domain.enums.PaymentStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class SimulatedPaymentFactory {
    private SimulatedPaymentFactory() { }

    public static SimulatedPayment create(UUID buyerId, MarketplaceOrder order,
                                          PaymentOption paymentOption, PaymentScenario scenario,
                                          String idempotencyKey) {
        String key = idempotencyKey == null ? null : idempotencyKey.trim();
        if (buyerId == null || order == null || paymentOption == null || scenario == null || key == null
                || key.isBlank() || key.length() > 120) return null;
        PaymentStatus status = switch (scenario) {
            case SUCCESS -> PaymentStatus.SUCCEEDED;
            case DECLINED, INSUFFICIENT_FUNDS -> PaymentStatus.DECLINED;
            case TIMEOUT -> PaymentStatus.FAILED;
        };
        String failure = switch (scenario) {
            case SUCCESS -> null;
            case DECLINED -> "DECLINED";
            case INSUFFICIENT_FUNDS -> "INSUFFICIENT_FUNDS";
            case TIMEOUT -> "TIMEOUT";
        };
        String reference = "PAY-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 18).toUpperCase(Locale.ROOT);
        return new SimulatedPayment.Builder().setId(Helper.generateId()).setBuyerId(buyerId)
                .setOrderId(order.getId()).setAmount(order.getTotalAmount()).setCurrency(order.getCurrency())
                .setPaymentOption(paymentOption).setScenario(scenario).setStatus(status)
                .setResultingOrderStatus(order.getStatus()).setIdempotencyKey(key).setReference(reference)
                .setFailureCode(failure).setProcessedAt(Instant.now()).build();
    }
}
