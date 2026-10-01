package com.example.unimarket.repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Narrow, database-side projections used by the administrator overview. */
public interface IAdminOverviewRepository {
    Snapshot readOverview(Instant asOf, int queueLimit, int activityLimit);

    record Snapshot(
            Map<String, Long> accountStatuses,
            long emailVerifiedAccounts,
            long currentlyLockedAccounts,
            Map<String, Long> vendorStatuses,
            Map<String, Long> productStatuses,
            Map<String, Long> orderStatuses,
            Map<String, Long> paymentStatuses,
            Map<String, Long> reportStatuses,
            Map<String, Long> escrowStatuses,
            Map<String, Long> bulletinStatuses,
            long bulletinEvents,
            long visibleBulletinPosts,
            long reviewCount,
            Double averageRating,
            long pointEntries,
            long pointsAwarded,
            long badgeAwards,
            long engagedUsers,
            Map<String, Long> pointEntriesByTrack,
            Map<String, Long> notificationTypes,
            long unreadNotifications,
            long totalSessions,
            long activeSessions,
            long expiredSessions,
            long revokedSessions,
            Map<String, Long> sessionRevocations,
            List<VendorApprovalRow> vendorApprovals,
            List<ReportQueueRow> openReports,
            List<EscrowQueueRow> disputedEscrow,
            List<ActivityRow> recentActivity
    ) {
    }

    record VendorApprovalRow(
            UUID vendorId,
            UUID userId,
            String businessName,
            String vendorType,
            Instant submittedAt
    ) {
    }

    record ReportQueueRow(
            UUID reportId,
            String targetType,
            String reason,
            Instant createdAt
    ) {
    }

    record EscrowQueueRow(
            UUID escrowId,
            String status,
            Instant disputedAt
    ) {
    }

    record ActivityRow(
            String resourceType,
            UUID resourceId,
            String event,
            Instant occurredAt
    ) {
    }
}
