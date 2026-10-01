package com.example.unimarket.factory;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.ReportReason;
import com.example.unimarket.domain.enums.ReportResolution;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.ReportTargetType;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class SuspiciousActivityReportFactory {
    private SuspiciousActivityReportFactory() { }

    public static SuspiciousActivityReport create(UUID reporterId, ReportTargetType targetType,
                                                  UUID targetId, UUID targetOwnerId,
                                                  ReportReason reason, String details,
                                                  String targetSnapshot) {
        String cleanDetails = Helper.cleanText(details);
        String cleanSnapshot = Helper.cleanText(targetSnapshot);
        if (reporterId == null || targetType == null || targetId == null || reason == null
                || cleanDetails == null || cleanDetails.length() > 2000
                || cleanSnapshot == null || cleanSnapshot.length() > 2500) return null;
        String dedupeKey = reporterId + ":" + targetType + ":" + targetId + ":" + reason;
        if (dedupeKey.length() > 180) return null;
        return new SuspiciousActivityReport.Builder().setId(Helper.generateId()).setReporterId(reporterId)
                .setTargetType(targetType).setTargetId(targetId).setTargetOwnerId(targetOwnerId)
                .setReason(reason).setDetails(cleanDetails).setTargetSnapshot(cleanSnapshot)
                .setStatus(ReportStatus.OPEN).setActiveDedupeKey(dedupeKey).build();
    }

    public static SuspiciousActivityReport resolve(SuspiciousActivityReport report, UUID moderatorId,
                                                   ReportResolution resolution, ModerationAction action,
                                                   String note, String reporterMessage) {
        String cleanNote = Helper.cleanText(note);
        String cleanMessage = Helper.cleanText(reporterMessage);
        if (report == null || cleanNote == null || cleanNote.length() > 1000
                || cleanMessage != null && cleanMessage.length() > 500) return null;
        return report.resolve(moderatorId, resolution, action, cleanNote, cleanMessage, Instant.now())
                ? report : null;
    }
}
