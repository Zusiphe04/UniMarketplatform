package com.example.unimarket.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.unimarket.domain.enums.FulfillmentMethod;
import com.example.unimarket.domain.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "marketplace_order", uniqueConstraints = {
        @UniqueConstraint(name = "uk_marketplace_order_reference", columnNames = "reference"),
        @UniqueConstraint(name = "uk_marketplace_order_buyer_idempotency", columnNames = {"buyer_id", "idempotency_key"})
}, indexes = @Index(name = "idx_order_buyer_status", columnList = "buyer_id, status, placed_at"))
public class MarketplaceOrder extends AuditableEntity {
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;
    @Column(name = "reference", nullable = false, length = 40) private String reference;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private BigDecimal totalAmount;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Column(name = "item_count", nullable = false) private int itemCount;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30) private OrderStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_method", nullable = false, length = 30) private FulfillmentMethod fulfillmentMethod;
    @Column(name = "delivery_address", length = 500) private String deliveryAddress;
    @Column(name = "idempotency_key", length = 120) private String idempotencyKey;
    @Column(name = "checkout_request_fingerprint", length = 64, columnDefinition = "CHAR(64)") private String checkoutRequestFingerprint;
    @Column(name = "reservation_active", nullable = false) private boolean reservationActive;
    @Column(name = "reservation_expires_at") private Instant reservationExpiresAt;
    @Column(name = "placed_at", nullable = false) private Instant placedAt;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "held_at") private Instant heldAt;
    @Column(name = "disputed_at") private Instant disputedAt;
    @Column(name = "released_at") private Instant releasedAt;
    @Column(name = "refunded_at") private Instant refundedAt;
    @Column(name = "payment_failure_code", length = 50) private String paymentFailureCode;

    protected MarketplaceOrder() { super(); }
    private MarketplaceOrder(Builder b) {
        super(b.id); buyerId = b.buyerId; reference = b.reference; totalAmount = b.totalAmount; currency = b.currency;
        itemCount = b.itemCount; status = b.status; fulfillmentMethod = b.fulfillmentMethod; deliveryAddress = b.deliveryAddress;
        idempotencyKey = b.idempotencyKey; checkoutRequestFingerprint = b.checkoutRequestFingerprint;
        reservationActive = b.reservationActive; reservationExpiresAt = b.reservationExpiresAt; placedAt = b.placedAt;
        paidAt = b.paidAt; heldAt = b.heldAt; disputedAt = b.disputedAt; releasedAt = b.releasedAt;
        refundedAt = b.refundedAt; paymentFailureCode = b.paymentFailureCode;
    }
    public UUID getBuyerId() { return buyerId; }
    public String getReference() { return reference; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public int getItemCount() { return itemCount; }
    public OrderStatus getStatus() { return status; }
    public FulfillmentMethod getFulfillmentMethod() { return fulfillmentMethod; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getCheckoutRequestFingerprint() { return checkoutRequestFingerprint; }
    public boolean isReservationActive() { return reservationActive; }
    public Instant getReservationExpiresAt() { return reservationExpiresAt; }
    public Instant getPlacedAt() { return placedAt; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getHeldAt() { return heldAt; }
    public Instant getDisputedAt() { return disputedAt; }
    public Instant getReleasedAt() { return releasedAt; }
    public Instant getRefundedAt() { return refundedAt; }
    public String getPaymentFailureCode() { return paymentFailureCode; }
    public boolean activateReservation(Instant expiresAt) {
        if (!awaitingPayment() || reservationActive || expiresAt == null) return false;
        reservationActive = true; reservationExpiresAt = expiresAt; return true;
    }
    public boolean deactivateReservation() {
        if (!reservationActive) return false;
        reservationActive = false; return true;
    }
    public boolean markPaid(Instant at) {
        if (!awaitingPayment() || at == null) return false;
        status = OrderStatus.PAID; paidAt = at; paymentFailureCode = null; return true;
    }
    public boolean markHeld(Instant at) {
        if (!awaitingPayment() || at == null) return false;
        status = OrderStatus.HELD; paidAt = at; heldAt = at; paymentFailureCode = null; return true;
    }
    public boolean confirmReceipt(Instant at) {
        if (status != OrderStatus.HELD || at == null) return false;
        status = OrderStatus.RELEASED; releasedAt = at; return true;
    }
    public boolean openDispute(Instant at) {
        if (status != OrderStatus.HELD || at == null) return false;
        status = OrderStatus.DISPUTED; disputedAt = at; return true;
    }
    public boolean resolveDispute(boolean release, Instant at) {
        if (status != OrderStatus.DISPUTED || at == null) return false;
        status = release ? OrderStatus.RELEASED : OrderStatus.REFUNDED;
        if (release) releasedAt = at; else refundedAt = at;
        return true;
    }
    public boolean markPaymentFailed(String code) {
        if (!awaitingPayment()) return false;
        status = OrderStatus.PAYMENT_FAILED; paymentFailureCode = code; return true;
    }
    public boolean cancel() {
        if (!awaitingPayment()) return false;
        status = OrderStatus.CANCELLED; return true;
    }
    private boolean awaitingPayment() {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAYMENT_FAILED;
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof MarketplaceOrder value && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id; private UUID buyerId; private String reference; private BigDecimal totalAmount; private String currency;
        private int itemCount; private OrderStatus status; private FulfillmentMethod fulfillmentMethod; private String deliveryAddress;
        private String idempotencyKey; private String checkoutRequestFingerprint; private boolean reservationActive;
        private Instant reservationExpiresAt; private Instant placedAt; private Instant paidAt; private Instant heldAt;
        private Instant disputedAt; private Instant releasedAt; private Instant refundedAt; private String paymentFailureCode;
        public Builder setId(UUID v) { id = v; return this; }
        public Builder setBuyerId(UUID v) { buyerId = v; return this; }
        public Builder setReference(String v) { reference = v; return this; }
        public Builder setTotalAmount(BigDecimal v) { totalAmount = v; return this; }
        public Builder setCurrency(String v) { currency = v; return this; }
        public Builder setItemCount(int v) { itemCount = v; return this; }
        public Builder setStatus(OrderStatus v) { status = v; return this; }
        public Builder setFulfillmentMethod(FulfillmentMethod v) { fulfillmentMethod = v; return this; }
        public Builder setDeliveryAddress(String v) { deliveryAddress = v; return this; }
        public Builder setIdempotencyKey(String v) { idempotencyKey = v; return this; }
        public Builder setCheckoutRequestFingerprint(String v) { checkoutRequestFingerprint = v; return this; }
        public Builder setReservationActive(boolean v) { reservationActive = v; return this; }
        public Builder setReservationExpiresAt(Instant v) { reservationExpiresAt = v; return this; }
        public Builder setPlacedAt(Instant v) { placedAt = v; return this; }
        public Builder setPaidAt(Instant v) { paidAt = v; return this; }
        public Builder setHeldAt(Instant v) { heldAt = v; return this; }
        public Builder setDisputedAt(Instant v) { disputedAt = v; return this; }
        public Builder setReleasedAt(Instant v) { releasedAt = v; return this; }
        public Builder setRefundedAt(Instant v) { refundedAt = v; return this; }
        public Builder setPaymentFailureCode(String v) { paymentFailureCode = v; return this; }
        public Builder copy(MarketplaceOrder v) {
            return setId(v.getId()).setBuyerId(v.getBuyerId()).setReference(v.getReference())
                    .setTotalAmount(v.getTotalAmount()).setCurrency(v.getCurrency()).setItemCount(v.getItemCount())
                    .setStatus(v.getStatus()).setFulfillmentMethod(v.getFulfillmentMethod()).setDeliveryAddress(v.getDeliveryAddress())
                    .setIdempotencyKey(v.getIdempotencyKey()).setCheckoutRequestFingerprint(v.getCheckoutRequestFingerprint())
                    .setReservationActive(v.isReservationActive()).setReservationExpiresAt(v.getReservationExpiresAt())
                    .setPlacedAt(v.getPlacedAt()).setPaidAt(v.getPaidAt()).setHeldAt(v.getHeldAt())
                    .setDisputedAt(v.getDisputedAt()).setReleasedAt(v.getReleasedAt()).setRefundedAt(v.getRefundedAt())
                    .setPaymentFailureCode(v.getPaymentFailureCode());
        }
        public MarketplaceOrder build() { return new MarketplaceOrder(this); }
    }
}
