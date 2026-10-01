package com.example.unimarket.controller;

import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.NotificationResponse;
import com.example.unimarket.response.UnreadNotificationCountResponse;
import com.example.unimarket.service.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final INotificationService notificationService;
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.list(userId(jwt), page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationService.unreadCount(userId(jwt)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) {
        return ResponseEntity.ok(notificationService.markRead(userId(jwt), notificationId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        int count = notificationService.markAllRead(userId(jwt));
        return ResponseEntity.ok(MessageResponse.of(count + " notification(s) marked as read."));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
