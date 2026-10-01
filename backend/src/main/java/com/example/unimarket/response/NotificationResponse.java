package com.example.unimarket.response;

import com.example.unimarket.domain.Notification;
import com.example.unimarket.domain.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String resourceType,
        UUID resourceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getResourceType(), notification.getResourceId(),
                notification.isRead(), notification.getReadAt(), notification.getCreatedAt());
    }
}
