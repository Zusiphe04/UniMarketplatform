package com.example.unimarket.factory;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.enums.EscrowResolution;
import com.example.unimarket.domain.enums.FulfillmentMethod;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.util.Helper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class MarketplaceOrderFactory {
    public static final String CURRENCY = "ZAR";
    private MarketplaceOrderFactory() { }

    public static MarketplaceOrder create(UUID buyerId, BigDecimal total, int itemCount,
                                          FulfillmentMethod method, String deliveryAddress) {
        return create(buyerId, total, itemCount, method, deliveryAddress, null, null);
    }

    public static MarketplaceOrder create(UUID buyerId, BigDecimal total, int itemCount,
                                          FulfillmentMethod method, String deliveryAddress,
                                          String idempotencyKey, String requestFingerprint) {
        String address = Helper.cleanText(deliveryAddress);
        if (buyerId == null || total == null || total.signum() <= 0 || itemCount < 1 || method == null
                || method == FulfillmentMethod.LOCAL_DELIVERY && address == null
                || address != null && address.length() > 500
                || idempotencyKey != null && (idempotencyKey.isBlank() || idempotencyKey.length() > 120)
                || requestFingerprint != null && requestFingerprint.length() != 64) return null;
        Instant now = Instant.now();
        String reference = "UM-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 16).toUpperCase(Locale.ROOT);
        return new MarketplaceOrder.Builder().setId(Helper.generateId()).setBuyerId(buyerId)
                .setReference(reference).setTotalAmount(total.setScale(2)).setCurrency(CURRENCY)
                .setItemCount(itemCount).setStatus(OrderStatus.PENDING_PAYMENT)
                .setFulfillmentMethod(method).setDeliveryAddress(address).setIdempotencyKey(idempotencyKey)
                .setCheckoutRequestFingerprint(requestFingerprint).setPlacedAt(now).build();
    }

    public static MarketplaceOrder markPaid(MarketplaceOrder order) {
        return order != null && order.markPaid(Instant.now()) ? order : null;
    }
    public static MarketplaceOrder markHeld(MarketplaceOrder order) {
        return order != null && order.markHeld(Instant.now()) ? order : null;
    }
    public static MarketplaceOrder confirmReceipt(MarketplaceOrder order) {
        return order != null && order.confirmReceipt(Instant.now()) ? order : null;
    }
    public static MarketplaceOrder openDispute(MarketplaceOrder order) {
        return order != null && order.openDispute(Instant.now()) ? order : null;
    }
    public static MarketplaceOrder resolveDispute(MarketplaceOrder order, EscrowResolution resolution) {
        return order != null && resolution != null
                && order.resolveDispute(resolution == EscrowResolution.RELEASE, Instant.now()) ? order : null;
    }
    public static MarketplaceOrder markPaymentFailed(MarketplaceOrder order, String failureCode) {
        String code = Helper.cleanText(failureCode);
        return order != null && code != null && code.length() <= 50 && order.markPaymentFailed(code)
                ? order : null;
    }
}
