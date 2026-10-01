package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.ReportReason;
import com.example.unimarket.domain.enums.ReportResolution;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.ReportTargetType;
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
@Table(name = "suspicious_activity_report",
        uniqueConstraints = @UniqueConstraint(name = "uk_report_active_dedupe", columnNames = "active_dedupe_key"),
        indexes = {
                @Index(name = "idx_report_reporter_created", columnList = "reporter_id, created_at"),
                @Index(name = "idx_report_queue", columnList = "status, created_at"),
                @Index(name = "idx_report_target", columnList = "target_type, target_id")
        })
public class SuspiciousActivityReport extends AuditableEntity {
    @Column(name = "reporter_id", nullable = false) private UUID reporterId;
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30) private ReportTargetType targetType;
    @Column(name = "target_id", nullable = false) private UUID targetId;
    @Column(name = "target_owner_id") private UUID targetOwnerId;
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30) private ReportReason reason;
    @Column(name = "details", nullable = false, length = 2000) private String details;
    @Column(name = "target_snapshot", nullable = false, length = 2500) private String targetSnapshot;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) private ReportStatus status;
    @Column(name = "active_dedupe_key", length = 180) private String activeDedupeKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", length = 30) private ReportResolution resolution;
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_action", length = 30) private ModerationAction moderationAction;
    @Column(name = "resolved_by_user_id") private UUID resolvedByUserId;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolution_note", length = 1000) private String resolutionNote;
    @Column(name = "reporter_message", length = 500) private String reporterMessage;

    protected SuspiciousActivityReport() { super(); }
    private SuspiciousActivityReport(Builder builder) {
        super(builder.id); reporterId = builder.reporterId; targetType = builder.targetType;
        targetId = builder.targetId; targetOwnerId = builder.targetOwnerId; reason = builder.reason;
        details = builder.details; targetSnapshot = builder.targetSnapshot; status = builder.status;
        activeDedupeKey = builder.activeDedupeKey; resolution = builder.resolution;
        moderationAction = builder.moderationAction; resolvedByUserId = builder.resolvedByUserId;
        resolvedAt = builder.resolvedAt; resolutionNote = builder.resolutionNote;
        reporterMessage = builder.reporterMessage;
    }
    public UUID getReporterId() { return reporterId; }
    public ReportTargetType getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public UUID getTargetOwnerId() { return targetOwnerId; }
    public ReportReason getReason() { return reason; }
    public String getDetails() { return details; }
    public String getTargetSnapshot() { return targetSnapshot; }
    public ReportStatus getStatus() { return status; }
    public String getActiveDedupeKey() { return activeDedupeKey; }
    public ReportResolution getResolution() { return resolution; }
    public ModerationAction getModerationAction() { return moderationAction; }
    public UUID getResolvedByUserId() { return resolvedByUserId; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public String getReporterMessage() { return reporterMessage; }
    public boolean resolve(UUID moderatorId, ReportResolution result, ModerationAction action,
                           String note, String message, Instant at) {
        if (status != ReportStatus.OPEN || moderatorId == null || result == null || action == null
                || note == null || at == null) return false;
        status = result == ReportResolution.ACTION_TAKEN || result == ReportResolution.ESCALATED
                ? ReportStatus.RESOLVED : ReportStatus.DISMISSED;
        resolution = result; moderationAction = action; resolvedByUserId = moderatorId;
        resolvedAt = at; resolutionNote = note; reporterMessage = message; activeDedupeKey = null;
        return true;
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof SuspiciousActivityReport value
                && getId() != null && getId().equals(value.getId());
    }
    @Override public int hashCode() { return Objects.hashCode(getId()); }
    public static class Builder {
        private UUID id; private UUID reporterId; private ReportTargetType targetType; private UUID targetId;
        private UUID targetOwnerId; private ReportReason reason; private String details; private String targetSnapshot;
        private ReportStatus status; private String activeDedupeKey; private ReportResolution resolution;
        private ModerationAction moderationAction; private UUID resolvedByUserId; private Instant resolvedAt;
        private String resolutionNote; private String reporterMessage;
        public Builder setId(UUID value) { id = value; return this; }
        public Builder setReporterId(UUID value) { reporterId = value; return this; }
        public Builder setTargetType(ReportTargetType value) { targetType = value; return this; }
        public Builder setTargetId(UUID value) { targetId = value; return this; }
        public Builder setTargetOwnerId(UUID value) { targetOwnerId = value; return this; }
        public Builder setReason(ReportReason value) { reason = value; return this; }
        public Builder setDetails(String value) { details = value; return this; }
        public Builder setTargetSnapshot(String value) { targetSnapshot = value; return this; }
        public Builder setStatus(ReportStatus value) { status = value; return this; }
        public Builder setActiveDedupeKey(String value) { activeDedupeKey = value; return this; }
        public Builder setResolution(ReportResolution value) { resolution = value; return this; }
        public Builder setModerationAction(ModerationAction value) { moderationAction = value; return this; }
        public Builder setResolvedByUserId(UUID value) { resolvedByUserId = value; return this; }
        public Builder setResolvedAt(Instant value) { resolvedAt = value; return this; }
        public Builder setResolutionNote(String value) { resolutionNote = value; return this; }
        public Builder setReporterMessage(String value) { reporterMessage = value; return this; }
        public SuspiciousActivityReport build() { return new SuspiciousActivityReport(this); }
    }
}
