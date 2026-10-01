package com.example.unimarket.controller;

import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.request.ResolveReportRequest;
import com.example.unimarket.response.ModerationReportResponse;
import com.example.unimarket.service.ISuspiciousActivityReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/reports")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationReportController {
    private final ISuspiciousActivityReportService reportService;

    public ModerationReportController(ISuspiciousActivityReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<Page<ModerationReportResponse>> queue(
            @RequestParam(defaultValue = "OPEN") ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.listQueue(status, page, size));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ModerationReportResponse> get(@PathVariable UUID reportId) {
        return ResponseEntity.ok(reportService.getForModeration(reportId));
    }

    @PatchMapping("/{reportId}/resolve")
    public ResponseEntity<ModerationReportResponse> resolve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID reportId,
            @Valid @RequestBody ResolveReportRequest request) {
        return ResponseEntity.ok(reportService.resolve(userId(jwt), reportId, request));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
