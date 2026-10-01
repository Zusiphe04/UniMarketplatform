package com.example.unimarket.service;

import com.example.unimarket.request.OpenEscrowDisputeRequest;
import com.example.unimarket.request.ResolveEscrowRequest;
import com.example.unimarket.response.EscrowResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IEscrowService {
    EscrowResponse getBuyerEscrow(UUID buyerId, UUID orderId);
    EscrowResponse confirmReceipt(UUID buyerId, UUID orderId);
    EscrowResponse openDispute(UUID buyerId, UUID orderId, OpenEscrowDisputeRequest request);
    Page<EscrowResponse> listDisputes(int page, int size);
    EscrowResponse resolve(UUID moderatorId, UUID orderId, ResolveEscrowRequest request);
}
