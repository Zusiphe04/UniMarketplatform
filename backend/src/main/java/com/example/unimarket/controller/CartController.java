package com.example.unimarket.controller;

import com.example.unimarket.request.AddCartItemRequest;
import com.example.unimarket.request.UpdateCartItemRequest;
import com.example.unimarket.response.CartResponse;
import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.service.ICartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('BUYER') and !hasRole('ADMIN')")
public class CartController {
    private final ICartService cartService;
    public CartController(ICartService cartService) { this.cartService = cartService; }

    @GetMapping public ResponseEntity<CartResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cartService.getCart(userId(jwt)));
    }
    @PostMapping("/items") public ResponseEntity<CartResponse> add(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId(jwt), request));
    }
    @PutMapping("/items/{productId}") public ResponseEntity<CartResponse> update(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userId(jwt), productId, request));
    }
    @DeleteMapping("/items/{productId}") public ResponseEntity<CartResponse> remove(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(userId(jwt), productId));
    }
    @DeleteMapping public ResponseEntity<MessageResponse> clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(userId(jwt));
        return ResponseEntity.ok(MessageResponse.of("Cart cleared."));
    }
    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
