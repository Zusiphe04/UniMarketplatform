package com.example.unimarket.controller;

import com.example.unimarket.domain.enums.VendorVerificationStatus;
import com.example.unimarket.request.ReviewVendorProfileRequest;
import com.example.unimarket.response.VendorProfileResponse;
import com.example.unimarket.service.IVendorProfileService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vendors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVendorController {
    private final IVendorProfileService vendorService;
    public AdminVendorController(IVendorProfileService vendorService) { this.vendorService = vendorService; }

    @GetMapping
    public ResponseEntity<List<VendorProfileResponse>> list(
            @RequestParam(required = false) VendorVerificationStatus status) {
        return ResponseEntity.ok(vendorService.listForReview(status));
    }

    @PatchMapping("/{userId}/review")
    public ResponseEntity<VendorProfileResponse> review(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @Valid @RequestBody ReviewVendorProfileRequest request) {
        return ResponseEntity.ok(vendorService.review(userId, request, currentUserId(jwt)));
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
