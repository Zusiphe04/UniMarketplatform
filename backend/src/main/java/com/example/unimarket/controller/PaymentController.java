package com.example.unimarket.controller;

import com.example.unimarket.request.SimulatePaymentRequest;
import com.example.unimarket.response.PaymentOptionResponse;
import com.example.unimarket.response.SimulatedPaymentResponse;
import com.example.unimarket.service.ISimulatedPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
public class PaymentController {
    private final ISimulatedPaymentService paymentService;
    public PaymentController(ISimulatedPaymentService paymentService) { this.paymentService = paymentService; }

    @GetMapping("/options")
    public ResponseEntity<List<PaymentOptionResponse>> options() {
        return ResponseEntity.ok(paymentService.options());
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulatedPaymentResponse> simulate(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody SimulatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.simulate(userId(jwt), idempotencyKey, request));
    }
    @GetMapping public ResponseEntity<List<SimulatedPaymentResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(paymentService.list(userId(jwt)));
    }
    @GetMapping("/{paymentId}") public ResponseEntity<SimulatedPaymentResponse> get(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.get(userId(jwt), paymentId));
    }
    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
