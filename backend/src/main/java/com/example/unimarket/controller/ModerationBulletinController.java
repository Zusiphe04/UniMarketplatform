package com.example.unimarket.controller;

import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.service.IBulletinPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/bulletin")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationBulletinController {
    private final IBulletinPostService bulletinService;
    public ModerationBulletinController(IBulletinPostService bulletinService) {
        this.bulletinService = bulletinService;
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<MessageResponse> remove(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        bulletinService.moderateRemove(userId(jwt), postId);
        return ResponseEntity.ok(MessageResponse.of("Bulletin post removed by moderation."));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
