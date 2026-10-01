package com.example.unimarket.controller;

import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.response.EngagementProfileResponse;
import com.example.unimarket.response.LeaderboardEntryResponse;
import com.example.unimarket.response.LoyaltyPointResponse;
import com.example.unimarket.service.IEngagementService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class EngagementController {
    private final IEngagementService engagementService;

    public EngagementController(IEngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping("/engagement/me")
    public ResponseEntity<EngagementProfileResponse> profile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(engagementService.getProfile(userId(jwt)));
    }

    @GetMapping("/engagement/me/points")
    public ResponseEntity<Page<LoyaltyPointResponse>> points(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(engagementService.listPoints(userId(jwt), page, size));
    }

    @GetMapping("/leaderboards/{track}")
    public ResponseEntity<Page<LeaderboardEntryResponse>> leaderboard(
            @PathVariable EngagementTrack track,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(engagementService.leaderboard(track, page, size));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
