package com.example.unimarket.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bounded, redacted operational view for the administrator dashboard.
 *
 * <p>The response deliberately contains aggregate values and controlled enum labels only.
 * Authentication secrets, addresses, report evidence, notification content, device data,
 * and unrestricted member-owned records are never projected into this model.
 */
public record AdminOverviewResponse(
        Instant generatedAt,
        AccountSummary accounts,
        LifecycleSummary vendors,
        LifecycleSummary products,
        LifecycleSummary orders,
        LifecycleSummary simulatedPayments,
        LifecycleSummary reports,
        LifecycleSummary simulatedEscrow,
        BulletinSummary bulletin,
        ReviewSummary reviews,
        EngagementSummary engagement,
        NotificationSummary notifications,
        SessionSummary sessions,
        QueueSummary queues,
        List<RecentActivity> recentActivity
) {
    public record LifecycleSummary(long total, Map<String, Long> byStatus) {
    }

    public record AccountSummary(
            long total,
            Map<String, Long> byStatus,
            long emailVerified,
            long currentlyLocked
    ) {
    }

    public record BulletinSummary(
            long total,
            Map<String, Long> byStatus,
            long events,
            long currentlyVisible
    ) {
    }

    public record ReviewSummary(long total, Double averageRating) {
    }

    public record EngagementSummary(
            long pointEntries,
            long pointsAwarded,
            long badgeAwards,
            long engagedUsers,
            Map<String, Long> pointEntriesByTrack
    ) {
    }

    public record NotificationSummary(
            long total,
            long unread,
            long read,
            Map<String, Long> byType
    ) {
    }

    public record SessionSummary(
            long total,
            long active,
            long expired,
            long revoked,
            Map<String, Long> revocationsByReason
    ) {
    }

    public record QueueSummary(
            long pendingVendors,
            List<VendorApprovalItem> vendorApprovals,
            long openReports,
            List<ReportQueueItem> reports,
            long disputedEscrow,
            List<EscrowQueueItem> escrow
    ) {
    }

    public record VendorApprovalItem(
            UUID vendorId,
            UUID userId,
            String businessName,
            String vendorType,
            Instant submittedAt
    ) {
    }

    public record ReportQueueItem(
            UUID reportId,
            String targetType,
            String reason,
            Instant createdAt
    ) {
    }

    public record EscrowQueueItem(
            UUID escrowId,
            String status,
            Instant disputedAt
    ) {
    }

    public record RecentActivity(
            String resourceType,
            UUID resourceId,
            String event,
            Instant occurredAt
    ) {
    }
}
