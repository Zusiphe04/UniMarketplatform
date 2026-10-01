package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentScenario;
import com.example.unimarket.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Credential-free result of an allowlisted payment simulation. */
@Entity
@Table(name = "order_payment_simulation",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_buyer_idempotency", columnNames = {"buyer_id", "idempotency_key"}),
                @UniqueConstraint(name = "uk_payment_reference", columnNames = "reference")
        },
        indexes = @Index(name = "idx_payment_order", columnList = "order_id, processed_at"))
public class SimulatedPayment extends AuditableEntity {
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_option", length = 20) private PaymentOption paymentOption;
    @Enumerated(EnumType.STRING)
    @Column(name = "scenario", nullable = false, length = 30) private PaymentScenario scenario;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) private PaymentStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_order_status", length = 30) private OrderStatus resultingOrderStatus;
    @Column(name = "idempotency_key", nullable = false, length = 120) private String idempotencyKey;
    @Column(name = "reference", nullable = false, length = 50) private String reference;
    @Column(name = "failure_code", length = 50) private String failureCode;
    @Column(name = "processed_at", nullable = false) private Instant processedAt;

    protected SimulatedPayment() { super(); }
    private SimulatedPayment(Builder builder) {
        super(builder.id); buyerId = builder.buyerId; orderId = builder.orderId; amount = builder.amount;
        currency = builder.currency; paymentOption = builder.paymentOption;
        scenario = builder.scenario; status = builder.status; resultingOrderStatus = builder.resultingOrderStatus;
        idempotencyKey = builder.idempotencyKey; reference = builder.reference;
        failureCode = builder.failureCode; processedAt = builder.processedAt;
    }
    public UUID getBuyerId() { return buyerId; }
    public UUID getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentOption getPaymentOption() { return paymentOption; }
    public PaymentScenario getScenario() { return scenario; }
    public PaymentStatus getStatus() { return status; }
    public OrderStatus getResultingOrderStatus() { return resultingOrderStatus; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getReference() { return reference; }
    public String getFailureCode() { return failureCode; }
    public Instant getProcessedAt() { return processedAt; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof SimulatedPayment value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    @Override public String toString() {
        return "SimulatedPayment{id=" + getId() + ", orderId=" + orderId
                + ", paymentOption=" + paymentOption + ", status=" + status
                + ", reference='" + reference + "'}";
    }
    public static class Builder {
        private UUID id; private UUID buyerId; private UUID orderId; private BigDecimal amount;
        private String currency; private PaymentOption paymentOption;
        private PaymentScenario scenario; private PaymentStatus status; private OrderStatus resultingOrderStatus;
        private String idempotencyKey; private String reference; private String failureCode; private Instant processedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setBuyerId(UUID value) { buyerId = value; return this; }
        public Builder setOrderId(UUID value) { orderId = value; return this; }
        public Builder setAmount(BigDecimal value) { amount = value; return this; }
        public Builder setCurrency(String value) { currency = value; return this; }
        public Builder setPaymentOption(PaymentOption value) { paymentOption = value; return this; }
        public Builder setScenario(PaymentScenario value) { scenario = value; return this; }
        public Builder setStatus(PaymentStatus value) { status = value; return this; }
        public Builder setResultingOrderStatus(OrderStatus value) { resultingOrderStatus = value; return this; }
        public Builder setIdempotencyKey(String value) { idempotencyKey = value; return this; }
        public Builder setReference(String value) { reference = value; return this; }
        public Builder setFailureCode(String value) { failureCode = value; return this; }
        public Builder setProcessedAt(Instant value) { processedAt = value; return this; }
        public Builder copy(SimulatedPayment value) { return setId(value.getId()).setBuyerId(value.getBuyerId())
                .setOrderId(value.getOrderId()).setAmount(value.getAmount()).setCurrency(value.getCurrency())
                .setPaymentOption(value.getPaymentOption()).setScenario(value.getScenario())
                .setStatus(value.getStatus()).setResultingOrderStatus(value.getResultingOrderStatus())
                .setIdempotencyKey(value.getIdempotencyKey()).setReference(value.getReference())
                .setFailureCode(value.getFailureCode()).setProcessedAt(value.getProcessedAt()); }
        public SimulatedPayment build() { return new SimulatedPayment(this); }
    }
}
