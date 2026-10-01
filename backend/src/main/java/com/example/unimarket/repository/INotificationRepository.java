package com.example.unimarket.repository;

import com.example.unimarket.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface INotificationRepository extends IRepository<Notification, UUID> {
    Page<Notification> readByRecipientId(UUID recipientId, Pageable pageable);
    Notification readByRecipientIdAndId(UUID recipientId, UUID id);
    Notification readByRecipientIdAndEventKey(UUID recipientId, String eventKey);
    long countUnread(UUID recipientId);
    int markAllRead(UUID recipientId, Instant readAt);
}
