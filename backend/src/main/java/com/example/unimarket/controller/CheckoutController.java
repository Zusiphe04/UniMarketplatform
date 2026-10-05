package com.example.unimarket.controller;

import com.example.unimarket.request.CheckoutRequest;
import com.example.unimarket.response.OrderResponse;
import com.example.unimarket.service.IOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout")
@PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
public class CheckoutController {
    private final IOrderService orderService;
    public CheckoutController(IOrderService orderService) { this.orderService = orderService; }

    /**
     * A successful replay deliberately keeps the existing 201 response contract
     * and returns the order created by the original request.
     */
    @PostMapping public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(userId(jwt), idempotencyKey, request));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
