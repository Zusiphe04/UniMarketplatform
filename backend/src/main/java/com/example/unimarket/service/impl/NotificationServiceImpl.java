package com.example.unimarket.service.impl;

import com.example.unimarket.domain.Notification;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.NotificationFactory;
import com.example.unimarket.repository.INotificationRepository;
import com.example.unimarket.response.NotificationResponse;
import com.example.unimarket.response.UnreadNotificationCountResponse;
import com.example.unimarket.service.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements INotificationService {
    private final INotificationRepository notificationRepository;

    public NotificationServiceImpl(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void send(UUID recipientId, NotificationType type, String title, String message,
                     String resourceType, UUID resourceId, String eventKey) {
        if (notificationRepository.readByRecipientIdAndEventKey(recipientId, eventKey) != null) return;
        Notification notification = NotificationFactory.create(recipientId, type, title, message,
                resourceType, resourceId, eventKey);
        if (notification == null) throw new ValidationException("The notification is invalid.");
        notificationRepository.create(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID recipientId, int page, int size) {
        if (page < 0 || size < 1) throw new ValidationException("Invalid notification page request.");
        return notificationRepository.readByRecipientId(recipientId,
                PageRequest.of(page, Math.min(size, 100))).map(NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse unreadCount(UUID recipientId) {
        return new UnreadNotificationCountResponse(notificationRepository.countUnread(recipientId));
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID recipientId, UUID notificationId) {
        Notification notification = notificationRepository.readByRecipientIdAndId(recipientId, notificationId);
        if (notification == null) throw ResourceNotFoundException.of("Notification");
        if (notification.markRead(Instant.now())) notificationRepository.update(notification);
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public int markAllRead(UUID recipientId) {
        return notificationRepository.markAllRead(recipientId, Instant.now());
    }
}
