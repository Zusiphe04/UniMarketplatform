package com.example.unimarket.response;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.ReportReason;
import com.example.unimarket.domain.enums.ReportResolution;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.ReportTargetType;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        ReportTargetType targetType,
        UUID targetId,
        ReportReason reason,
        String details,
        ReportStatus status,
        ReportResolution resolution,
        ModerationAction moderationAction,
        String reporterMessage,
        Instant createdAt,
        Instant resolvedAt
) {
    public static ReportResponse from(SuspiciousActivityReport report) {
        return new ReportResponse(report.getId(), report.getTargetType(), report.getTargetId(),
                report.getReason(), report.getDetails(), report.getStatus(), report.getResolution(),
                report.getModerationAction(), report.getReporterMessage(), report.getCreatedAt(),
                report.getResolvedAt());
    }
}
