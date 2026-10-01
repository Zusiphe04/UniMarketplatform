package com.example.unimarket.response;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.ReportReason;
import com.example.unimarket.domain.enums.ReportResolution;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.ReportTargetType;

import java.time.Instant;
import java.util.UUID;

public record ModerationReportResponse(
        UUID id,
        UUID reporterId,
        ReportTargetType targetType,
        UUID targetId,
        UUID targetOwnerId,
        ReportReason reason,
        String details,
        String targetSnapshot,
        ReportStatus status,
        ReportResolution resolution,
        ModerationAction moderationAction,
        UUID resolvedByUserId,
        String resolutionNote,
        String reporterMessage,
        Instant createdAt,
        Instant resolvedAt
) {
    public static ModerationReportResponse from(SuspiciousActivityReport report) {
        return new ModerationReportResponse(report.getId(), report.getReporterId(), report.getTargetType(),
                report.getTargetId(), report.getTargetOwnerId(), report.getReason(), report.getDetails(),
                report.getTargetSnapshot(), report.getStatus(), report.getResolution(),
                report.getModerationAction(), report.getResolvedByUserId(), report.getResolutionNote(),
                report.getReporterMessage(), report.getCreatedAt(), report.getResolvedAt());
    }
}
