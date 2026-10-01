package com.example.unimarket.service.impl;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductReview;
import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.ReportResolution;
import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.domain.enums.ReportTargetType;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.SuspiciousActivityReportFactory;
import com.example.unimarket.repository.IBulletinPostRepository;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.repository.IProductReviewRepository;
import com.example.unimarket.repository.ISuspiciousActivityReportRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.request.CreateReportRequest;
import com.example.unimarket.request.ResolveReportRequest;
import com.example.unimarket.response.ModerationReportResponse;
import com.example.unimarket.response.ReportResponse;
import com.example.unimarket.service.IBulletinPostService;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IProductReviewService;
import com.example.unimarket.service.ISuspiciousActivityReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SuspiciousActivityReportServiceImpl implements ISuspiciousActivityReportService {
    private final ISuspiciousActivityReportRepository reportRepository;
    private final IUserAccountRepository accountRepository;
    private final IProductRepository productRepository;
    private final IProductReviewRepository reviewRepository;
    private final IBulletinPostRepository bulletinRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IBulletinPostService bulletinService;
    private final IProductReviewService reviewService;
    private final INotificationService notificationService;

    public SuspiciousActivityReportServiceImpl(
            ISuspiciousActivityReportRepository reportRepository,
            IUserAccountRepository accountRepository,
            IProductRepository productRepository,
            IProductReviewRepository reviewRepository,
            IBulletinPostRepository bulletinRepository,
            IMarketplaceOrderRepository orderRepository,
            IOrderItemRepository orderItemRepository,
            IBulletinPostService bulletinService,
            IProductReviewService reviewService,
            INotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.bulletinRepository = bulletinRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bulletinService = bulletinService;
        this.reviewService = reviewService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ReportResponse create(UUID reporterId, CreateReportRequest request) {
        UserAccount reporter = accountRepository.read(reporterId);
        if (reporter == null || reporter.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can submit reports.");
        }
        TargetEvidence evidence = targetEvidence(reporterId, request.targetType(), request.targetId());
        SuspiciousActivityReport report = SuspiciousActivityReportFactory.create(
                reporterId, request.targetType(), request.targetId(), evidence.ownerId(),
                request.reason(), request.details(), evidence.snapshot());
        if (report == null) throw new ValidationException("The suspicious-activity report is invalid.");
        return ReportResponse.from(reportRepository.create(report));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> listMine(UUID reporterId, int page, int size) {
        return reportRepository.readByReporterId(reporterId, pageRequest(page, size)).map(ReportResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getMine(UUID reporterId, UUID reportId) {
        SuspiciousActivityReport report = reportRepository.readByIdAndReporterId(reportId, reporterId);
        if (report == null) throw ResourceNotFoundException.of("Report");
        return ReportResponse.from(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationReportResponse> listQueue(ReportStatus status, int page, int size) {
        ReportStatus filter = status == null ? ReportStatus.OPEN : status;
        return reportRepository.readByStatus(filter, pageRequest(page, size)).map(ModerationReportResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ModerationReportResponse getForModeration(UUID reportId) {
        SuspiciousActivityReport report = reportRepository.read(reportId);
        if (report == null) throw ResourceNotFoundException.of("Report");
        return ModerationReportResponse.from(report);
    }

    @Override
    @Transactional
    public ModerationReportResponse resolve(UUID moderatorId, UUID reportId, ResolveReportRequest request) {
        SuspiciousActivityReport report = reportRepository.readByIdForUpdate(reportId);
        if (report == null) throw ResourceNotFoundException.of("Report");
        if (report.getStatus() != ReportStatus.OPEN) {
            if (report.getResolution() == request.resolution()
                    && report.getModerationAction() == request.action()) {
                return ModerationReportResponse.from(report);
            }
            throw new ValidationException("This report has already been resolved.");
        }
        validateResolution(report, request);
        applyAction(moderatorId, report, request.action());
        SuspiciousActivityReport resolved = SuspiciousActivityReportFactory.resolve(report, moderatorId,
                request.resolution(), request.action(), request.note(), request.reporterMessage());
        if (resolved == null) throw new ValidationException("The report resolution is invalid.");
        SuspiciousActivityReport saved = reportRepository.update(resolved);
        String message = saved.getReporterMessage() == null
                ? "Your report was reviewed. Outcome: " + saved.getResolution().name().replace('_', ' ').toLowerCase() + "."
                : saved.getReporterMessage();
        notificationService.send(saved.getReporterId(), NotificationType.REPORT_RESOLVED,
                "Report reviewed", message, "REPORT", saved.getId(),
                "report:" + saved.getId() + ":resolved");
        return ModerationReportResponse.from(saved);
    }

    private void validateResolution(SuspiciousActivityReport report, ResolveReportRequest request) {
        switch (request.action()) {
            case BULLETIN_REMOVED -> {
                if (report.getTargetType() != ReportTargetType.BULLETIN_POST
                        || request.resolution() != ReportResolution.ACTION_TAKEN) {
                    throw new ValidationException("BULLETIN_REMOVED requires a bulletin report and ACTION_TAKEN.");
                }
            }
            case REVIEW_REMOVED -> {
                if (report.getTargetType() != ReportTargetType.PRODUCT_REVIEW
                        || request.resolution() != ReportResolution.ACTION_TAKEN) {
                    throw new ValidationException("REVIEW_REMOVED requires a review report and ACTION_TAKEN.");
                }
            }
            case ESCALATED -> {
                if (request.resolution() != ReportResolution.ESCALATED) {
                    throw new ValidationException("ESCALATED action requires the ESCALATED resolution.");
                }
            }
            case NONE -> {
                if (request.resolution() == ReportResolution.ACTION_TAKEN
                        || request.resolution() == ReportResolution.ESCALATED) {
                    throw new ValidationException("This resolution requires a matching moderation action.");
                }
            }
        }
    }

    private void applyAction(UUID moderatorId, SuspiciousActivityReport report, ModerationAction action) {
        switch (action) {
            case BULLETIN_REMOVED -> bulletinService.moderateRemove(moderatorId, report.getTargetId());
            case REVIEW_REMOVED -> reviewService.moderateDelete(report.getTargetId());
            case NONE, ESCALATED -> { }
        }
    }

    private TargetEvidence targetEvidence(UUID reporterId, ReportTargetType type, UUID targetId) {
        return switch (type) {
            case USER_ACCOUNT -> userEvidence(reporterId, targetId);
            case PRODUCT -> productEvidence(reporterId, targetId);
            case PRODUCT_REVIEW -> reviewEvidence(reporterId, targetId);
            case BULLETIN_POST -> bulletinEvidence(reporterId, targetId);
            case MARKETPLACE_ORDER -> orderEvidence(reporterId, targetId);
        };
    }

    private TargetEvidence userEvidence(UUID reporterId, UUID targetId) {
        UserAccount target = accountRepository.read(targetId);
        if (target == null) throw ResourceNotFoundException.of("Report target");
        if (targetId.equals(reporterId)) throw new ValidationException("You cannot report your own account.");
        return new TargetEvidence(targetId, "User account " + targetId + "; status=" + target.getStatus());
    }

    private TargetEvidence productEvidence(UUID reporterId, UUID targetId) {
        Product product = productRepository.readPublicById(targetId);
        if (product == null) throw ResourceNotFoundException.of("Report target");
        if (product.getSellerId().equals(reporterId)) throw new ValidationException("You cannot report your own product.");
        return new TargetEvidence(product.getSellerId(), "Product title=" + product.getTitle()
                + "; status=" + product.getStatus() + "; category=" + product.getCategory());
    }

    private TargetEvidence reviewEvidence(UUID reporterId, UUID targetId) {
        ProductReview review = reviewRepository.read(targetId);
        Product product = review == null ? null : productRepository.readPublicById(review.getProductId());
        if (review == null || product == null) throw ResourceNotFoundException.of("Report target");
        if (review.getReviewerId().equals(reporterId)) throw new ValidationException("You cannot report your own review.");
        return new TargetEvidence(review.getReviewerId(), truncate("Review rating=" + review.getRating()
                + "; productId=" + review.getProductId() + "; comment=" + review.getComment(), 2500));
    }

    private TargetEvidence bulletinEvidence(UUID reporterId, UUID targetId) {
        BulletinPost post = bulletinRepository.read(targetId);
        Instant now = Instant.now();
        if (post == null || post.getStatus() != BulletinPostStatus.PUBLISHED
                || post.getExpiresAt() != null && !post.getExpiresAt().isAfter(now)) {
            throw ResourceNotFoundException.of("Report target");
        }
        if (post.getAuthorId().equals(reporterId)) throw new ValidationException("You cannot report your own bulletin post.");
        return new TargetEvidence(post.getAuthorId(), truncate("Bulletin title=" + post.getTitle()
                + "; type=" + post.getType() + "; content=" + post.getContent(), 2500));
    }

    private TargetEvidence orderEvidence(UUID reporterId, UUID targetId) {
        MarketplaceOrder order = orderRepository.read(targetId);
        if (order == null) throw ResourceNotFoundException.of("Report target");
        boolean participant = order.getBuyerId().equals(reporterId)
                || orderItemRepository.existsByOrderIdAndSellerId(targetId, reporterId);
        if (!participant) throw ResourceNotFoundException.of("Report target");
        return new TargetEvidence(null, "Order reference=" + order.getReference() + "; status="
                + order.getStatus() + "; amount=" + order.getTotalAmount() + " " + order.getCurrency());
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1) throw new ValidationException("Invalid report page request.");
        return PageRequest.of(page, Math.min(size, 100));
    }

    private String truncate(String value, int maximum) {
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }

    private record TargetEvidence(UUID ownerId, String snapshot) { }
}
