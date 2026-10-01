package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;
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

/** Immutable, idempotent entry in a member's loyalty-points ledger. */
@Entity
@Table(name = "loyalty_point_entry",
        uniqueConstraints = @UniqueConstraint(name = "uk_loyalty_user_track_event",
                columnNames = {"user_id", "track", "event_key"}),
        indexes = {
                @Index(name = "idx_loyalty_user_track_time", columnList = "user_id, track, occurred_at"),
                @Index(name = "idx_loyalty_track_time", columnList = "track, occurred_at")
        })
public class LoyaltyPointEntry extends AuditableEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "track", nullable = false, length = 20) private EngagementTrack track;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40) private LoyaltyEventType eventType;
    @Column(name = "source_type", nullable = false, length = 40) private String sourceType;
    @Column(name = "source_id", nullable = false) private UUID sourceId;
    @Column(name = "event_key", nullable = false, length = 180) private String eventKey;
    @Column(name = "points", nullable = false) private int points;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected LoyaltyPointEntry() { super(); }
    private LoyaltyPointEntry(Builder builder) {
        super(builder.id); userId = builder.userId; track = builder.track; eventType = builder.eventType;
        sourceType = builder.sourceType; sourceId = builder.sourceId; eventKey = builder.eventKey;
        points = builder.points; occurredAt = builder.occurredAt;
    }
    public UUID getUserId() { return userId; }
    public EngagementTrack getTrack() { return track; }
    public LoyaltyEventType getEventType() { return eventType; }
    public String getSourceType() { return sourceType; }
    public UUID getSourceId() { return sourceId; }
    public String getEventKey() { return eventKey; }
    public int getPoints() { return points; }
    public Instant getOccurredAt() { return occurredAt; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof LoyaltyPointEntry value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID userId; private EngagementTrack track; private LoyaltyEventType eventType;
        private String sourceType; private UUID sourceId; private String eventKey; private int points;
        private Instant occurredAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setUserId(UUID value) { userId = value; return this; }
        public Builder setTrack(EngagementTrack value) { track = value; return this; }
        public Builder setEventType(LoyaltyEventType value) { eventType = value; return this; }
        public Builder setSourceType(String value) { sourceType = value; return this; }
        public Builder setSourceId(UUID value) { sourceId = value; return this; }
        public Builder setEventKey(String value) { eventKey = value; return this; }
        public Builder setPoints(int value) { points = value; return this; }
        public Builder setOccurredAt(Instant value) { occurredAt = value; return this; }
        public LoyaltyPointEntry build() { return new LoyaltyPointEntry(this); }
    }
}
