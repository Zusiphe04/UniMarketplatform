package com.example.unimarket.repository;

import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.EscrowStatus;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.domain.enums.PaymentStatus;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.domain.enums.VendorVerificationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AdminOverviewRepositoryImpl implements IAdminOverviewRepository {
    private static final int MAX_QUEUE_LIMIT = 10;
    private static final int MAX_ACTIVITY_LIMIT = 50;

    private final EntityManager entityManager;

    public AdminOverviewRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Snapshot readOverview(Instant asOf, int queueLimit, int activityLimit) {
        if (asOf == null) throw new IllegalArgumentException("Overview time is required.");
        int boundedQueueLimit = Math.max(1, Math.min(queueLimit, MAX_QUEUE_LIMIT));
        int boundedActivityLimit = Math.max(1, Math.min(activityLimit, MAX_ACTIVITY_LIMIT));

        Map<String, Long> accountStatuses = enumCounts("UserAccount", "status", AccountStatus.class);
        Map<String, Long> vendorStatuses = enumCounts(
                "VendorProfile", "verificationStatus", VendorVerificationStatus.class);
        Map<String, Long> productStatuses = enumCounts("Product", "status", ProductStatus.class);
        Map<String, Long> orderStatuses = enumCounts("MarketplaceOrder", "status", OrderStatus.class);
        Map<String, Long> paymentStatuses = enumCounts("SimulatedPayment", "status", PaymentStatus.class);
        Map<String, Long> reportStatuses = enumCounts(
                "SuspiciousActivityReport", "status", ReportStatus.class);
        Map<String, Long> escrowStatuses = enumCounts("SimulatedEscrow", "status", EscrowStatus.class);
        Map<String, Long> bulletinStatuses = enumCounts(
                "BulletinPost", "status", BulletinPostStatus.class);
        Map<String, Long> pointEntriesByTrack = enumCounts(
                "LoyaltyPointEntry", "track", EngagementTrack.class);
        Map<String, Long> notificationTypes = enumCounts(
                "Notification", "type", NotificationType.class);
        Map<String, Long> sessionRevocations = enumCounts(
                "AuthSession", "revocationReason", RevocationReason.class);

        return new Snapshot(
                accountStatuses,
                count("select count(a) from UserAccount a where a.emailVerifiedAt is not null"),
                count("select count(a) from UserAccount a where a.lockedUntil > :asOf", "asOf", asOf),
                vendorStatuses,
                productStatuses,
                orderStatuses,
                paymentStatuses,
                reportStatuses,
                escrowStatuses,
                bulletinStatuses,
                count("select count(b) from BulletinPost b where b.type = :type", "type", BulletinPostType.EVENT),
                count("select count(b) from BulletinPost b where b.status = :status "
                                + "and (b.expiresAt is null or b.expiresAt > :asOf)",
                        Map.of("status", BulletinPostStatus.PUBLISHED, "asOf", asOf)),
                count("select count(r) from ProductReview r"),
                average("select avg(r.rating) from ProductReview r"),
                count("select count(l) from LoyaltyPointEntry l"),
                number("select coalesce(sum(l.points), 0) from LoyaltyPointEntry l"),
                count("select count(b) from BadgeAward b"),
                count("select count(distinct l.userId) from LoyaltyPointEntry l"),
                pointEntriesByTrack,
                notificationTypes,
                count("select count(n) from Notification n where n.readAt is null"),
                count("select count(s) from AuthSession s"),
                count("select count(s) from AuthSession s where s.revokedAt is null and s.expiresAt > :asOf",
                        "asOf", asOf),
                count("select count(s) from AuthSession s where s.revokedAt is null and s.expiresAt <= :asOf",
                        "asOf", asOf),
                count("select count(s) from AuthSession s where s.revokedAt is not null"),
                sessionRevocations,
                readVendorApprovals(boundedQueueLimit),
                readOpenReports(boundedQueueLimit),
                readDisputedEscrow(boundedQueueLimit),
                readRecentActivity(boundedActivityLimit)
        );
    }

    private List<VendorApprovalRow> readVendorApprovals(int limit) {
        return entityManager.createQuery(
                        "select v.id, v.userId, v.businessName, v.vendorType, v.submittedAt "
                                + "from VendorProfile v where v.verificationStatus = :status "
                                + "order by v.submittedAt asc, v.id asc",
                        Object[].class)
                .setParameter("status", VendorVerificationStatus.PENDING)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(row -> new VendorApprovalRow(
                        (UUID) row[0], (UUID) row[1], (String) row[2], enumName(row[3]), (Instant) row[4]))
                .toList();
    }

    private List<ReportQueueRow> readOpenReports(int limit) {
        return entityManager.createQuery(
                        "select r.id, r.targetType, r.reason, r.createdAt "
                                + "from SuspiciousActivityReport r where r.status = :status "
                                + "order by r.createdAt asc, r.id asc",
                        Object[].class)
                .setParameter("status", ReportStatus.OPEN)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(row -> new ReportQueueRow(
                        (UUID) row[0], enumName(row[1]), enumName(row[2]), (Instant) row[3]))
                .toList();
    }

    private List<EscrowQueueRow> readDisputedEscrow(int limit) {
        return entityManager.createQuery(
                        "select e.id, e.status, e.disputedAt from SimulatedEscrow e "
                                + "where e.status = :status order by e.disputedAt asc, e.id asc",
                        Object[].class)
                .setParameter("status", EscrowStatus.DISPUTED)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(row -> new EscrowQueueRow(
                        (UUID) row[0], enumName(row[1]), (Instant) row[2]))
                .toList();
    }

    private List<ActivityRow> readRecentActivity(int limit) {
        List<ActivityRow> rows = new ArrayList<>();
        addEnumActivity(rows, "UserAccount", "status", "createdAt", "ACCOUNT", limit);
        addEnumActivity(rows, "VendorProfile", "verificationStatus", "submittedAt", "VENDOR", limit);
        addEnumActivity(rows, "Product", "status", "createdAt", "PRODUCT", limit);
        addEnumActivity(rows, "MarketplaceOrder", "status", "placedAt", "ORDER", limit);
        addEnumActivity(rows, "SimulatedPayment", "status", "processedAt", "SIMULATED_PAYMENT", limit);
        addEnumActivity(rows, "SuspiciousActivityReport", "status", "createdAt", "REPORT", limit);
        addEnumActivity(rows, "SimulatedEscrow", "status", "heldAt", "SIMULATED_ESCROW", limit);
        addEnumActivity(rows, "BulletinPost", "status", "createdAt", "BULLETIN", limit);
        addEnumActivity(rows, "LoyaltyPointEntry", "eventType", "occurredAt", "ENGAGEMENT", limit);
        addEnumActivity(rows, "BadgeAward", "badgeCode", "awardedAt", "BADGE", limit);
        addConstantActivity(rows, "ProductReview", "createdAt", "REVIEW", "CREATED", limit);

        return rows.stream()
                .filter(row -> row.occurredAt() != null)
                .sorted(Comparator.comparing(ActivityRow::occurredAt).reversed())
                .limit(limit)
                .toList();
    }

    private void addEnumActivity(List<ActivityRow> target, String entityName, String eventProperty,
                                 String timeProperty, String resourceType, int limit) {
        entityManager.createQuery(
                        "select e.id, e." + eventProperty + ", e." + timeProperty + " from "
                                + entityName + " e order by e." + timeProperty + " desc, e.id desc",
                        Object[].class)
                .setMaxResults(limit)
                .getResultList()
                .forEach(row -> target.add(new ActivityRow(
                        resourceType, (UUID) row[0], enumName(row[1]), (Instant) row[2])));
    }

    private void addConstantActivity(List<ActivityRow> target, String entityName, String timeProperty,
                                     String resourceType, String event, int limit) {
        entityManager.createQuery(
                        "select e.id, e." + timeProperty + " from " + entityName
                                + " e order by e." + timeProperty + " desc, e.id desc",
                        Object[].class)
                .setMaxResults(limit)
                .getResultList()
                .forEach(row -> target.add(new ActivityRow(
                        resourceType, (UUID) row[0], event, (Instant) row[1])));
    }

    private Map<String, Long> enumCounts(
            String entityName, String property, Class<? extends Enum<?>> enumType) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Enum<?> value : enumType.getEnumConstants()) counts.put(value.name(), 0L);

        entityManager.createQuery(
                        "select e." + property + ", count(e) from " + entityName
                                + " e group by e." + property,
                        Object[].class)
                .getResultList()
                .forEach(row -> {
                    if (row[0] != null) counts.put(enumName(row[0]), ((Number) row[1]).longValue());
                });
        return Collections.unmodifiableMap(counts);
    }

    private long count(String jpql) {
        return number(jpql);
    }

    private long count(String jpql, String parameter, Object value) {
        Query query = entityManager.createQuery(jpql).setParameter(parameter, value);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long count(String jpql, Map<String, Object> parameters) {
        Query query = entityManager.createQuery(jpql);
        parameters.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long number(String jpql) {
        Object value = entityManager.createQuery(jpql).getSingleResult();
        return value == null ? 0L : ((Number) value).longValue();
    }

    private Double average(String jpql) {
        Object value = entityManager.createQuery(jpql).getSingleResult();
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static String enumName(Object value) {
        return value instanceof Enum<?> enumValue ? enumValue.name() : String.valueOf(value);
    }
}
