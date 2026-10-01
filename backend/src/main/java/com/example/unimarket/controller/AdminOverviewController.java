package com.example.unimarket.controller;

import com.example.unimarket.response.AdminOverviewResponse;
import com.example.unimarket.service.IAdminOverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOverviewController {
    private final IAdminOverviewService overviewService;

    public AdminOverviewController(IAdminOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> getOverview() {
        return ResponseEntity.ok(overviewService.getOverview());
    }
}
