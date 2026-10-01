package com.example.unimarket.controller;

import com.example.unimarket.request.CreateReportRequest;
import com.example.unimarket.response.ReportResponse;
import com.example.unimarket.service.ISuspiciousActivityReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/reports")
@PreAuthorize("isAuthenticated()")
public class SuspiciousActivityReportController {
    private final ISuspiciousActivityReportService reportService;

    public SuspiciousActivityReportController(ISuspiciousActivityReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(userId(jwt), request));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<ReportResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.listMine(userId(jwt), page, size));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> get(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID reportId) {
        return ResponseEntity.ok(reportService.getMine(userId(jwt), reportId));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
