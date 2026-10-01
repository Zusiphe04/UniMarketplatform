package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_notification",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_recipient_event", columnNames = {"recipient_id", "event_key"}),
        indexes = @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at"))
public class Notification extends AuditableEntity {
    @Column(name = "recipient_id", nullable = false) private UUID recipientId;
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40) private NotificationType type;
    @Column(name = "title", nullable = false, length = 160) private String title;
    @Column(name = "message", nullable = false, length = 500) private String message;
    @Column(name = "resource_type", nullable = false, length = 40) private String resourceType;
    @Column(name = "resource_id") private UUID resourceId;
    @Column(name = "event_key", nullable = false, length = 180) private String eventKey;
    @Column(name = "read_at") private Instant readAt;

    protected Notification() { super(); }
    private Notification(Builder builder) {
        super(builder.id);
        recipientId = builder.recipientId;
        type = builder.type;
        title = builder.title;
        message = builder.message;
        resourceType = builder.resourceType;
        resourceId = builder.resourceId;
        eventKey = builder.eventKey;
        readAt = builder.readAt;
    }

    public UUID getRecipientId() { return recipientId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getEventKey() { return eventKey; }
    public Instant getReadAt() { return readAt; }
    public boolean isRead() { return readAt != null; }
    public boolean markRead(Instant now) {
        if (readAt != null || now == null) return false;
        readAt = now;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof Notification value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id;
        private UUID recipientId;
        private NotificationType type;
        private String title;
        private String message;
        private String resourceType;
        private UUID resourceId;
        private String eventKey;
        private Instant readAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setRecipientId(UUID value) { recipientId = value; return this; }
        public Builder setType(NotificationType value) { type = value; return this; }
        public Builder setTitle(String value) { title = value; return this; }
        public Builder setMessage(String value) { message = value; return this; }
        public Builder setResourceType(String value) { resourceType = value; return this; }
        public Builder setResourceId(UUID value) { resourceId = value; return this; }
        public Builder setEventKey(String value) { eventKey = value; return this; }
        public Builder setReadAt(Instant value) { readAt = value; return this; }
        public Builder copy(Notification value) {
            return setId(value.getId()).setRecipientId(value.getRecipientId()).setType(value.getType())
                    .setTitle(value.getTitle()).setMessage(value.getMessage())
                    .setResourceType(value.getResourceType()).setResourceId(value.getResourceId())
                    .setEventKey(value.getEventKey()).setReadAt(value.getReadAt());
        }
        public Notification build() { return new Notification(this); }
    }
}
