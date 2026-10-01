package com.example.unimarket.controller;

import com.example.unimarket.request.CreateReviewRequest;
import com.example.unimarket.request.RespondToReviewRequest;
import com.example.unimarket.request.UpdateReviewRequest;
import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.ProductRatingSummaryResponse;
import com.example.unimarket.response.ProductReviewResponse;
import com.example.unimarket.service.IProductReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ProductReviewController {
    private final IProductReviewService reviewService;

    public ProductReviewController(IProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Page<ProductReviewResponse>> list(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.list(productId, page, size));
    }

    @GetMapping("/products/{productId}/summary")
    public ResponseEntity<ProductRatingSummaryResponse> summary(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.summary(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProductReviewResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(userId(jwt), request));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProductReviewResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.update(userId(jwt), reviewId, request));
    }

    @PostMapping("/{reviewId}/response")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductReviewResponse> respond(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reviewId,
            @Valid @RequestBody RespondToReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.respond(userId(jwt), reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reviewId) {
        reviewService.delete(userId(jwt), reviewId);
        return ResponseEntity.ok(MessageResponse.of("Review removed."));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
