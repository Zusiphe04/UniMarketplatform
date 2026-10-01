package com.example.unimarket.response;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowResolution;
import com.example.unimarket.domain.enums.EscrowStatus;
import com.example.unimarket.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EscrowResponse(
        UUID id,
        UUID orderId,
        String orderReference,
        UUID buyerId,
        BigDecimal amount,
        String currency,
        EscrowStatus status,
        OrderStatus orderStatus,
        Instant heldAt,
        Instant disputedAt,
        String disputeReason,
        EscrowResolution resolution,
        UUID resolvedByUserId,
        Instant resolvedAt,
        String resolutionNote
) {
    public static EscrowResponse from(SimulatedEscrow escrow, MarketplaceOrder order) {
        return new EscrowResponse(escrow.getId(), escrow.getOrderId(), order.getReference(),
                escrow.getBuyerId(), escrow.getAmount(), escrow.getCurrency(), escrow.getStatus(),
                order.getStatus(), escrow.getHeldAt(), escrow.getDisputedAt(), escrow.getDisputeReason(),
                escrow.getResolution(), escrow.getResolvedByUserId(), escrow.getResolvedAt(),
                escrow.getResolutionNote());
    }
}
