package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.EventRegistrationStatus;
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

/** One attendee's durable registration record for a hosted bulletin event. */
@Entity
@Table(name = "event_registration",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_registration_attendee",
                columnNames = {"bulletin_post_id", "attendee_id"}),
        indexes = {
                @Index(name = "idx_event_registration_queue", columnList = "bulletin_post_id, registration_status, queued_at"),
                @Index(name = "idx_event_registration_attendee", columnList = "attendee_id, registration_status, created_at")
        })
public class EventRegistration extends AuditableEntity {
    @Column(name = "bulletin_post_id", nullable = false) private UUID bulletinPostId;
    @Column(name = "attendee_id", nullable = false) private UUID attendeeId;
    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false, length = 20)
    private EventRegistrationStatus status;
    @Column(name = "queued_at", nullable = false) private Instant queuedAt;
    @Column(name = "confirmed_at") private Instant confirmedAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;

    protected EventRegistration() { super(); }

    private EventRegistration(Builder builder) {
        super(builder.id);
        bulletinPostId = builder.bulletinPostId;
        attendeeId = builder.attendeeId;
        status = builder.status;
        queuedAt = builder.queuedAt;
        confirmedAt = builder.confirmedAt;
        cancelledAt = builder.cancelledAt;
    }

    public UUID getBulletinPostId() { return bulletinPostId; }
    public UUID getAttendeeId() { return attendeeId; }
    public EventRegistrationStatus getStatus() { return status; }
    public Instant getQueuedAt() { return queuedAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public boolean isActive() {
        return status == EventRegistrationStatus.CONFIRMED || status == EventRegistrationStatus.WAITLISTED;
    }

    public boolean confirm(Instant now) {
        if (!isActive() || now == null) return false;
        status = EventRegistrationStatus.CONFIRMED;
        confirmedAt = now;
        cancelledAt = null;
        return true;
    }

    public boolean waitlist(Instant now) {
        if (!isActive() || now == null) return false;
        status = EventRegistrationStatus.WAITLISTED;
        queuedAt = now;
        confirmedAt = null;
        cancelledAt = null;
        return true;
    }

    public boolean reregister(EventRegistrationStatus nextStatus, Instant now) {
        if (isActive() || nextStatus == null || now == null) return false;
        status = nextStatus;
        queuedAt = now;
        confirmedAt = nextStatus == EventRegistrationStatus.CONFIRMED ? now : null;
        cancelledAt = null;
        return true;
    }

    public boolean cancel(Instant now, boolean eventCancelled) {
        if (!isActive() || now == null) return false;
        status = eventCancelled ? EventRegistrationStatus.EVENT_CANCELLED : EventRegistrationStatus.CANCELLED;
        cancelledAt = now;
        return true;
    }

    @Override public boolean equals(Object other) {
        return this == other || other instanceof EventRegistration value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }

    public static class Builder {
        private UUID id;
        private UUID bulletinPostId;
        private UUID attendeeId;
        private EventRegistrationStatus status;
        private Instant queuedAt;
        private Instant confirmedAt;
        private Instant cancelledAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setBulletinPostId(UUID value) { bulletinPostId = value; return this; }
        public Builder setAttendeeId(UUID value) { attendeeId = value; return this; }
        public Builder setStatus(EventRegistrationStatus value) { status = value; return this; }
        public Builder setQueuedAt(Instant value) { queuedAt = value; return this; }
        public Builder setConfirmedAt(Instant value) { confirmedAt = value; return this; }
        public Builder setCancelledAt(Instant value) { cancelledAt = value; return this; }
        public EventRegistration build() { return new EventRegistration(this); }
    }
}
