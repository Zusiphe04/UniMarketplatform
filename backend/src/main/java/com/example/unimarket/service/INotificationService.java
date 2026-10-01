package com.example.unimarket.service;

import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.response.NotificationResponse;
import com.example.unimarket.response.UnreadNotificationCountResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface INotificationService {
    void send(UUID recipientId, NotificationType type, String title, String message,
              String resourceType, UUID resourceId, String eventKey);
    Page<NotificationResponse> list(UUID recipientId, int page, int size);
    UnreadNotificationCountResponse unreadCount(UUID recipientId);
    NotificationResponse markRead(UUID recipientId, UUID notificationId);
    int markAllRead(UUID recipientId);
}
