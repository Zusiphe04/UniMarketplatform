package com.example.unimarket.controller;

import com.example.unimarket.request.ResolveEscrowRequest;
import com.example.unimarket.response.EscrowResponse;
import com.example.unimarket.service.IEscrowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/escrow")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationEscrowController {
    private final IEscrowService escrowService;

    public ModerationEscrowController(IEscrowService escrowService) {
        this.escrowService = escrowService;
    }

    @GetMapping("/disputes")
    public ResponseEntity<Page<EscrowResponse>> disputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(escrowService.listDisputes(page, size));
    }

    @PostMapping("/orders/{orderId}/resolve")
    public ResponseEntity<EscrowResponse> resolve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId,
            @Valid @RequestBody ResolveEscrowRequest request) {
        return ResponseEntity.ok(escrowService.resolve(userId(jwt), orderId, request));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
