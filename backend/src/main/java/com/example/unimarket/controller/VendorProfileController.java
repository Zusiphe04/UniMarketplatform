package com.example.unimarket.controller;

import com.example.unimarket.request.SubmitVendorProfileRequest;
import com.example.unimarket.response.VendorProfileResponse;
import com.example.unimarket.service.IVendorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account/vendor-profile")
public class VendorProfileController {
    private final IVendorProfileService vendorService;

    public VendorProfileController(IVendorProfileService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER') and !hasRole('ADMIN')")
    public ResponseEntity<VendorProfileResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(vendorService.getOwn(userId(jwt)));
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<VendorProfileResponse> submit(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody SubmitVendorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.submit(userId(jwt), request));
    }

    @PutMapping
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<VendorProfileResponse> resubmit(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody SubmitVendorProfileRequest request) {
        return ResponseEntity.ok(vendorService.submit(userId(jwt), request));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
