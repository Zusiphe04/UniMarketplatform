package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.EngagementTrack;
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
@Table(name = "badge_award",
        uniqueConstraints = @UniqueConstraint(name = "uk_badge_user_track_code",
                columnNames = {"user_id", "track", "badge_code"}),
        indexes = @Index(name = "idx_badge_user_track", columnList = "user_id, track, awarded_at"))
public class BadgeAward extends AuditableEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(name = "track", nullable = false, length = 20) private EngagementTrack track;
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_code", nullable = false, length = 40) private BadgeCode badgeCode;
    @Column(name = "awarded_at", nullable = false) private Instant awardedAt;

    protected BadgeAward() { super(); }
    private BadgeAward(Builder builder) {
        super(builder.id); userId = builder.userId; track = builder.track;
        badgeCode = builder.badgeCode; awardedAt = builder.awardedAt;
    }
    public UUID getUserId() { return userId; }
    public EngagementTrack getTrack() { return track; }
    public BadgeCode getBadgeCode() { return badgeCode; }
    public Instant getAwardedAt() { return awardedAt; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof BadgeAward value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID userId; private EngagementTrack track;
        private BadgeCode badgeCode; private Instant awardedAt;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setUserId(UUID value) { userId = value; return this; }
        public Builder setTrack(EngagementTrack value) { track = value; return this; }
        public Builder setBadgeCode(BadgeCode value) { badgeCode = value; return this; }
        public Builder setAwardedAt(Instant value) { awardedAt = value; return this; }
        public BadgeAward build() { return new BadgeAward(this); }
    }
}
