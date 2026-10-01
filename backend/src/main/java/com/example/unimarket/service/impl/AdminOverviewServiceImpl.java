package com.example.unimarket.service.impl;

import com.example.unimarket.repository.IAdminOverviewRepository;
import com.example.unimarket.response.AdminOverviewResponse;
import com.example.unimarket.service.IAdminOverviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AdminOverviewServiceImpl implements IAdminOverviewService {
    private static final int QUEUE_PREVIEW_LIMIT = 5;
    private static final int RECENT_ACTIVITY_LIMIT = 20;

    private final IAdminOverviewRepository overviewRepository;

    public AdminOverviewServiceImpl(IAdminOverviewRepository overviewRepository) {
        this.overviewRepository = overviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        Instant generatedAt = Instant.now();
        IAdminOverviewRepository.Snapshot snapshot = overviewRepository.readOverview(
                generatedAt, QUEUE_PREVIEW_LIMIT, RECENT_ACTIVITY_LIMIT);

        long notificationTotal = total(snapshot.notificationTypes());
        return new AdminOverviewResponse(
                generatedAt,
                new AdminOverviewResponse.AccountSummary(
                        total(snapshot.accountStatuses()), snapshot.accountStatuses(),
                        snapshot.emailVerifiedAccounts(), snapshot.currentlyLockedAccounts()),
                lifecycle(snapshot.vendorStatuses()),
                lifecycle(snapshot.productStatuses()),
                lifecycle(snapshot.orderStatuses()),
                lifecycle(snapshot.paymentStatuses()),
                lifecycle(snapshot.reportStatuses()),
                lifecycle(snapshot.escrowStatuses()),
                new AdminOverviewResponse.BulletinSummary(
                        total(snapshot.bulletinStatuses()), snapshot.bulletinStatuses(),
                        snapshot.bulletinEvents(), snapshot.visibleBulletinPosts()),
                new AdminOverviewResponse.ReviewSummary(snapshot.reviewCount(), snapshot.averageRating()),
                new AdminOverviewResponse.EngagementSummary(
                        snapshot.pointEntries(), snapshot.pointsAwarded(), snapshot.badgeAwards(),
                        snapshot.engagedUsers(), snapshot.pointEntriesByTrack()),
                new AdminOverviewResponse.NotificationSummary(
                        notificationTotal, snapshot.unreadNotifications(),
                        Math.max(0, notificationTotal - snapshot.unreadNotifications()),
                        snapshot.notificationTypes()),
                new AdminOverviewResponse.SessionSummary(
                        snapshot.totalSessions(), snapshot.activeSessions(), snapshot.expiredSessions(),
                        snapshot.revokedSessions(), snapshot.sessionRevocations()),
                new AdminOverviewResponse.QueueSummary(
                        count(snapshot.vendorStatuses(), "PENDING"),
                        snapshot.vendorApprovals().stream().map(row ->
                                new AdminOverviewResponse.VendorApprovalItem(
                                        row.vendorId(), row.userId(), row.businessName(),
                                        row.vendorType(), row.submittedAt())).toList(),
                        count(snapshot.reportStatuses(), "OPEN"),
                        snapshot.openReports().stream().map(row ->
                                new AdminOverviewResponse.ReportQueueItem(
                                        row.reportId(), row.targetType(), row.reason(), row.createdAt())).toList(),
                        count(snapshot.escrowStatuses(), "DISPUTED"),
                        snapshot.disputedEscrow().stream().map(row ->
                                new AdminOverviewResponse.EscrowQueueItem(
                                        row.escrowId(), row.status(), row.disputedAt())).toList()),
                snapshot.recentActivity().stream().map(row ->
                        new AdminOverviewResponse.RecentActivity(
                                row.resourceType(), row.resourceId(), row.event(), row.occurredAt())).toList()
        );
    }

    private static AdminOverviewResponse.LifecycleSummary lifecycle(Map<String, Long> counts) {
        return new AdminOverviewResponse.LifecycleSummary(total(counts), counts);
    }

    private static long total(Map<String, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long count(Map<String, Long> counts, String key) {
        return counts.getOrDefault(key, 0L);
    }
}
