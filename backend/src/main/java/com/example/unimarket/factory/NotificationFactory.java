package com.example.unimarket.factory;

import com.example.unimarket.domain.Notification;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.util.Helper;

import java.util.UUID;

public final class NotificationFactory {
    private NotificationFactory() { }

    public static Notification create(UUID recipientId, NotificationType type, String title, String message,
                                      String resourceType, UUID resourceId, String eventKey) {
        String cleanTitle = Helper.cleanText(title);
        String cleanMessage = Helper.cleanText(message);
        String cleanResourceType = Helper.cleanText(resourceType);
        String cleanEventKey = eventKey == null ? null : eventKey.trim();
        if (recipientId == null || type == null || cleanTitle == null || cleanTitle.length() > 160
                || cleanMessage == null || cleanMessage.length() > 500
                || cleanResourceType == null || cleanResourceType.length() > 40
                || cleanEventKey == null || cleanEventKey.length() > 180) return null;
        return new Notification.Builder().setId(Helper.generateId()).setRecipientId(recipientId)
                .setType(type).setTitle(cleanTitle).setMessage(cleanMessage)
                .setResourceType(cleanResourceType).setResourceId(resourceId)
                .setEventKey(cleanEventKey).build();
    }
}
