package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.EscrowResolution;
import com.example.unimarket.domain.enums.EscrowStatus;
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

/** Simulated escrow state only; no real funds are held or transferred. */
@Entity
@Table(name = "simulated_escrow",
        uniqueConstraints = @UniqueConstraint(name = "uk_escrow_order", columnNames = "order_id"),
        indexes = @Index(name = "idx_escrow_status_time", columnList = "status, disputed_at, held_at"))
public class SimulatedEscrow extends AuditableEntity {
    @Column(name = "order_id", nullable = false) private UUID orderId;
    @Column(name = "buyer_id", nullable = false) private UUID buyerId;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) private EscrowStatus status;
    @Column(name = "held_at", nullable = false) private Instant heldAt;
    @Column(name = "disputed_at") private Instant disputedAt;
    @Column(name = "dispute_reason", length = 1000) private String disputeReason;
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", length = 20) private EscrowResolution resolution;
    @Column(name = "resolved_by_user_id") private UUID resolvedByUserId;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolution_note", length = 1000) private String resolutionNote;

    protected SimulatedEscrow() { super(); }
    private SimulatedEscrow(Builder builder) {
        super(builder.id); orderId = builder.orderId; buyerId = builder.buyerId; amount = builder.amount;
        currency = builder.currency; status = builder.status; heldAt = builder.heldAt;
        disputedAt = builder.disputedAt; disputeReason = builder.disputeReason;
        resolution = builder.resolution; resolvedByUserId = builder.resolvedByUserId;
        resolvedAt = builder.resolvedAt; resolutionNote = builder.resolutionNote;
    }
    public UUID getOrderId() { return orderId; }
    public UUID getBuyerId() { return buyerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public EscrowStatus getStatus() { return status; }
    public Instant getHeldAt() { return heldAt; }
    public Instant getDisputedAt() { return disputedAt; }
    public String getDisputeReason() { return disputeReason; }
    public EscrowResolution getResolution() { return resolution; }
    public UUID getResolvedByUserId() { return resolvedByUserId; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public boolean confirmReceipt(UUID actorId, Instant at) {
        if (status != EscrowStatus.HELD || !buyerId.equals(actorId) || at == null) return false;
        status = EscrowStatus.RELEASED; resolution = EscrowResolution.RELEASE;
        resolvedByUserId = actorId; resolvedAt = at; resolutionNote = "Buyer confirmed receipt."; return true;
    }
    public boolean openDispute(String reason, Instant at) {
        if (status != EscrowStatus.HELD || reason == null || at == null) return false;
        status = EscrowStatus.DISPUTED; disputeReason = reason; disputedAt = at; return true;
    }
    public boolean resolve(UUID moderatorId, EscrowResolution outcome, String note, Instant at) {
        if (status != EscrowStatus.DISPUTED || moderatorId == null || outcome == null || note == null || at == null) {
            return false;
        }
        status = outcome == EscrowResolution.RELEASE ? EscrowStatus.RELEASED : EscrowStatus.REFUNDED;
        resolution = outcome; resolvedByUserId = moderatorId; resolvedAt = at; resolutionNote = note; return true;
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof SimulatedEscrow value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID orderId; private UUID buyerId; private BigDecimal amount;
        private String currency; private EscrowStatus status; private Instant heldAt; private Instant disputedAt;
        private String disputeReason; private EscrowResolution resolution; private UUID resolvedByUserId;
        private Instant resolvedAt; private String resolutionNote;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setOrderId(UUID value) { orderId = value; return this; }
        public Builder setBuyerId(UUID value) { buyerId = value; return this; }
        public Builder setAmount(BigDecimal value) { amount = value; return this; }
        public Builder setCurrency(String value) { currency = value; return this; }
        public Builder setStatus(EscrowStatus value) { status = value; return this; }
        public Builder setHeldAt(Instant value) { heldAt = value; return this; }
        public Builder setDisputedAt(Instant value) { disputedAt = value; return this; }
        public Builder setDisputeReason(String value) { disputeReason = value; return this; }
        public Builder setResolution(EscrowResolution value) { resolution = value; return this; }
        public Builder setResolvedByUserId(UUID value) { resolvedByUserId = value; return this; }
        public Builder setResolvedAt(Instant value) { resolvedAt = value; return this; }
        public Builder setResolutionNote(String value) { resolutionNote = value; return this; }
        public SimulatedEscrow build() { return new SimulatedEscrow(this); }
    }
}
