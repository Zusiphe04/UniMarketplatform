package com.example.unimarket.factory;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.SimulatedEscrow;
import com.example.unimarket.domain.enums.EscrowResolution;
import com.example.unimarket.domain.enums.EscrowStatus;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class SimulatedEscrowFactory {
    private SimulatedEscrowFactory() { }

    public static SimulatedEscrow createHeld(MarketplaceOrder order) {
        if (order == null || order.getStatus() != OrderStatus.HELD || order.getHeldAt() == null) return null;
        return new SimulatedEscrow.Builder().setId(Helper.generateId()).setOrderId(order.getId())
                .setBuyerId(order.getBuyerId()).setAmount(order.getTotalAmount()).setCurrency(order.getCurrency())
                .setStatus(EscrowStatus.HELD).setHeldAt(order.getHeldAt()).build();
    }
    public static SimulatedEscrow confirmReceipt(SimulatedEscrow escrow, UUID buyerId) {
        return escrow != null && escrow.confirmReceipt(buyerId, Instant.now()) ? escrow : null;
    }
    public static SimulatedEscrow openDispute(SimulatedEscrow escrow, String reason) {
        String cleanReason = Helper.cleanText(reason);
        return escrow != null && cleanReason != null && cleanReason.length() <= 1000
                && escrow.openDispute(cleanReason, Instant.now()) ? escrow : null;
    }
    public static SimulatedEscrow resolve(SimulatedEscrow escrow, UUID moderatorId,
                                          EscrowResolution outcome, String note) {
        String cleanNote = Helper.cleanText(note);
        return escrow != null && cleanNote != null && cleanNote.length() <= 1000
                && escrow.resolve(moderatorId, outcome, cleanNote, Instant.now()) ? escrow : null;
    }
}
