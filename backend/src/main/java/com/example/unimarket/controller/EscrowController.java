package com.example.unimarket.controller;

import com.example.unimarket.request.OpenEscrowDisputeRequest;
import com.example.unimarket.response.EscrowResponse;
import com.example.unimarket.service.IEscrowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/escrow/orders")
@PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
public class EscrowController {
    private final IEscrowService escrowService;

    public EscrowController(IEscrowService escrowService) {
        this.escrowService = escrowService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<EscrowResponse> get(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(escrowService.getBuyerEscrow(userId(jwt), orderId));
    }

    @PostMapping("/{orderId}/confirm-receipt")
    public ResponseEntity<EscrowResponse> confirmReceipt(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(escrowService.confirmReceipt(userId(jwt), orderId));
    }

    @PostMapping("/{orderId}/disputes")
    public ResponseEntity<EscrowResponse> dispute(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId,
            @Valid @RequestBody OpenEscrowDisputeRequest request) {
        return ResponseEntity.ok(escrowService.openDispute(userId(jwt), orderId, request));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
