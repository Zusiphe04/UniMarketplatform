package com.example.unimarket.controller;

import com.example.unimarket.response.OrderItemFulfillmentResponse;
import com.example.unimarket.response.OrderResponse;
import com.example.unimarket.service.IOrderItemFulfillmentService;
import com.example.unimarket.service.IOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
public class OrderController {
    private final IOrderService orderService;
    private final IOrderItemFulfillmentService fulfillmentService;
    public OrderController(IOrderService orderService, IOrderItemFulfillmentService fulfillmentService) {
        this.orderService = orderService;
        this.fulfillmentService = fulfillmentService;
    }
    @GetMapping public ResponseEntity<List<OrderResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.listBuyerOrders(userId(jwt)));
    }
    @GetMapping("/{orderId}") public ResponseEntity<OrderResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getBuyerOrder(userId(jwt), orderId));
    }
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelBuyerOrder(userId(jwt), orderId));
    }
    @GetMapping("/{orderId}/fulfillment")
    public ResponseEntity<List<OrderItemFulfillmentResponse>> fulfillment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(fulfillmentService.listForBuyer(userId(jwt), orderId));
    }
    @PostMapping("/{orderId}/items/{orderItemId}/confirm-receipt")
    public ResponseEntity<OrderItemFulfillmentResponse> confirmReceipt(@AuthenticationPrincipal Jwt jwt,
                                                                        @PathVariable UUID orderId, @PathVariable UUID orderItemId) {
        return ResponseEntity.ok(fulfillmentService.confirmReceipt(userId(jwt), orderId, orderItemId));
    }
    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
