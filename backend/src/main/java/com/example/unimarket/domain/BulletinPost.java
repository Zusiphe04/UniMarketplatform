package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "bulletin_post", indexes = {
        @Index(name = "idx_bulletin_status_created", columnList = "status, created_at"),
        @Index(name = "idx_bulletin_type_status", columnList = "post_type, status"),
        @Index(name = "idx_bulletin_author_created", columnList = "author_id, created_at")
})
public class BulletinPost extends AuditableEntity {
    @Column(name = "author_id", nullable = false) private UUID authorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20) private BulletinPostType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) private BulletinPostStatus status;
    @Column(name = "title", nullable = false, length = 160) private String title;
    @Column(name = "content", nullable = false, length = 5000) private String content;
    @Column(name = "location", length = 200) private String location;
    @Column(name = "cover_image_url", length = 1000) private String coverImageUrl;
    @Column(name = "event_starts_at") private Instant eventStartsAt;
    @Column(name = "event_capacity") private Integer eventCapacity;
    @Column(name = "event_cancelled_at") private Instant eventCancelledAt;
    @Column(name = "event_cancellation_reason", length = 500) private String eventCancellationReason;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "removed_by_user_id") private UUID removedByUserId;
    @Column(name = "removed_at") private Instant removedAt;

    protected BulletinPost() { super(); }

    private BulletinPost(Builder builder) {
        super(builder.id);
        authorId = builder.authorId;
        type = builder.type;
        status = builder.status;
        title = builder.title;
        content = builder.content;
        location = builder.location;
        coverImageUrl = builder.coverImageUrl;
        eventStartsAt = builder.eventStartsAt;
        eventCapacity = builder.eventCapacity;
        eventCancelledAt = builder.eventCancelledAt;
        eventCancellationReason = builder.eventCancellationReason;
        expiresAt = builder.expiresAt;
        publishedAt = builder.publishedAt;
        removedByUserId = builder.removedByUserId;
        removedAt = builder.removedAt;
    }

    public UUID getAuthorId() { return authorId; }
    public BulletinPostType getType() { return type; }
    public BulletinPostStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLocation() { return location; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Instant getEventStartsAt() { return eventStartsAt; }
    public Integer getEventCapacity() { return eventCapacity; }
    public Instant getEventCancelledAt() { return eventCancelledAt; }
    public String getEventCancellationReason() { return eventCancellationReason; }
    public boolean isEventCancelled() { return eventCancelledAt != null; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public UUID getRemovedByUserId() { return removedByUserId; }
    public Instant getRemovedAt() { return removedAt; }

    public boolean update(BulletinPostType newType, String newTitle, String newContent,
                          String newLocation, String newCoverImageUrl, Instant newEventStartsAt,
                          Integer newEventCapacity, Instant newExpiresAt) {
        if (status == BulletinPostStatus.ARCHIVED || status == BulletinPostStatus.REMOVED || isEventCancelled()) return false;
        type = newType;
        title = newTitle;
        content = newContent;
        location = newLocation;
        coverImageUrl = newCoverImageUrl;
        eventStartsAt = newEventStartsAt;
        eventCapacity = newEventCapacity;
        expiresAt = newExpiresAt;
        return true;
    }

    public boolean publish(Instant now) {
        if (status != BulletinPostStatus.DRAFT || now == null) return false;
        status = BulletinPostStatus.PUBLISHED;
        publishedAt = now;
        return true;
    }

    public boolean cancelEvent(String reason, Instant now) {
        if (type != BulletinPostType.EVENT || status != BulletinPostStatus.PUBLISHED || isEventCancelled()
                || reason == null || now == null) return false;
        eventCancelledAt = now;
        eventCancellationReason = reason;
        return true;
    }

    public boolean archive() {
        if (status == BulletinPostStatus.ARCHIVED || status == BulletinPostStatus.REMOVED) return false;
        status = BulletinPostStatus.ARCHIVED;
        return true;
    }

    public boolean remove(UUID moderatorId, Instant now) {
        if (status == BulletinPostStatus.REMOVED || moderatorId == null || now == null) return false;
        status = BulletinPostStatus.REMOVED;
        removedByUserId = moderatorId;
        removedAt = now;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof BulletinPost value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id;
        private UUID authorId;
        private BulletinPostType type;
        private BulletinPostStatus status;
        private String title;
        private String content;
        private String location;
        private String coverImageUrl;
        private Instant eventStartsAt;
        private Integer eventCapacity;
        private Instant eventCancelledAt;
        private String eventCancellationReason;
        private Instant expiresAt;
        private Instant publishedAt;
        private UUID removedByUserId;
        private Instant removedAt;

        public Builder setId(UUID value) { id = value; return this; }
        public Builder setAuthorId(UUID value) { authorId = value; return this; }
        public Builder setType(BulletinPostType value) { type = value; return this; }
        public Builder setStatus(BulletinPostStatus value) { status = value; return this; }
        public Builder setTitle(String value) { title = value; return this; }
        public Builder setContent(String value) { content = value; return this; }
        public Builder setLocation(String value) { location = value; return this; }
        public Builder setCoverImageUrl(String value) { coverImageUrl = value; return this; }
        public Builder setEventStartsAt(Instant value) { eventStartsAt = value; return this; }
        public Builder setEventCapacity(Integer value) { eventCapacity = value; return this; }
        public Builder setEventCancelledAt(Instant value) { eventCancelledAt = value; return this; }
        public Builder setEventCancellationReason(String value) { eventCancellationReason = value; return this; }
        public Builder setExpiresAt(Instant value) { expiresAt = value; return this; }
        public Builder setPublishedAt(Instant value) { publishedAt = value; return this; }
        public Builder setRemovedByUserId(UUID value) { removedByUserId = value; return this; }
        public Builder setRemovedAt(Instant value) { removedAt = value; return this; }
        public Builder copy(BulletinPost value) {
            return setId(value.getId()).setAuthorId(value.getAuthorId()).setType(value.getType())
                    .setStatus(value.getStatus()).setTitle(value.getTitle()).setContent(value.getContent())
                    .setLocation(value.getLocation()).setCoverImageUrl(value.getCoverImageUrl())
                    .setEventStartsAt(value.getEventStartsAt()).setEventCapacity(value.getEventCapacity())
                    .setEventCancelledAt(value.getEventCancelledAt())
                    .setEventCancellationReason(value.getEventCancellationReason()).setExpiresAt(value.getExpiresAt())
                    .setPublishedAt(value.getPublishedAt()).setRemovedByUserId(value.getRemovedByUserId())
                    .setRemovedAt(value.getRemovedAt());
        }
        public BulletinPost build() { return new BulletinPost(this); }
    }
}
