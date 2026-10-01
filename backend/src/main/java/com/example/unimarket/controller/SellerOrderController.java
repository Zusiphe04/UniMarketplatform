package com.example.unimarket.controller;

import com.example.unimarket.request.UpdateOrderItemFulfillmentRequest;
import com.example.unimarket.response.OrderItemFulfillmentResponse;
import com.example.unimarket.response.SellerOrderResponse;
import com.example.unimarket.service.IOrderItemFulfillmentService;
import com.example.unimarket.service.IOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/orders")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {
    private final IOrderService orderService;
    private final IOrderItemFulfillmentService fulfillmentService;
    public SellerOrderController(IOrderService orderService, IOrderItemFulfillmentService fulfillmentService) {
        this.orderService = orderService;
        this.fulfillmentService = fulfillmentService;
    }
    @GetMapping public ResponseEntity<List<SellerOrderResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.listSellerOrders(userId(jwt)));
    }
    @GetMapping("/{orderId}/fulfillment")
    public ResponseEntity<List<OrderItemFulfillmentResponse>> fulfillment(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(fulfillmentService.listForSeller(userId(jwt), orderId));
    }
    @PatchMapping("/{orderId}/items/{orderItemId}/fulfillment")
    public ResponseEntity<OrderItemFulfillmentResponse> updateFulfillment(@AuthenticationPrincipal Jwt jwt,
                                                                            @PathVariable UUID orderId,
                                                                            @PathVariable UUID orderItemId,
                                                                            @Valid @RequestBody UpdateOrderItemFulfillmentRequest request) {
        return ResponseEntity.ok(fulfillmentService.updateBySeller(userId(jwt), orderId, orderItemId, request));
    }
    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
