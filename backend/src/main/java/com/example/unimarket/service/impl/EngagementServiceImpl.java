package com.example.unimarket.service.impl;

import com.example.unimarket.domain.BadgeAward;
import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductReview;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.BadgeCode;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;
import com.example.unimarket.domain.enums.OrderStatus;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.BadgeAwardFactory;
import com.example.unimarket.factory.LoyaltyPointEntryFactory;
import com.example.unimarket.repository.IBadgeAwardRepository;
import com.example.unimarket.repository.ILoyaltyPointRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.repository.LoyaltyLeaderboardProjection;
import com.example.unimarket.response.BadgeResponse;
import com.example.unimarket.response.EngagementProfileResponse;
import com.example.unimarket.response.LeaderboardEntryResponse;
import com.example.unimarket.response.LoyaltyPointResponse;
import com.example.unimarket.service.IEngagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EngagementServiceImpl implements IEngagementService {
    private final ILoyaltyPointRepository pointRepository;
    private final IBadgeAwardRepository badgeRepository;
    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;

    public EngagementServiceImpl(ILoyaltyPointRepository pointRepository,
                                 IBadgeAwardRepository badgeRepository,
                                 IUserAccountRepository accountRepository,
                                 IUserProfileRepository profileRepository) {
        this.pointRepository = pointRepository;
        this.badgeRepository = badgeRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public void recordCompletedOrder(MarketplaceOrder order, List<OrderItem> items) {
        if (order == null || (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.RELEASED) || order.getPaidAt() == null
                || items == null || items.isEmpty()) return;
        record(order.getBuyerId(), LoyaltyEventType.PURCHASE_COMPLETED, "ORDER", order.getId(),
                "order:" + order.getId() + ":buyer", order.getPaidAt());
        items.stream().map(item -> item.getSellerId()).distinct().forEach(sellerId ->
                record(sellerId, LoyaltyEventType.SALE_COMPLETED, "ORDER", order.getId(),
                        "order:" + order.getId() + ":seller:" + sellerId, order.getPaidAt()));
    }

    @Override
    @Transactional
    public void recordReviewCreated(ProductReview review) {
        if (review == null || review.getCreatedAt() == null) return;
        record(review.getReviewerId(), LoyaltyEventType.VERIFIED_REVIEW_CREATED, "PRODUCT_REVIEW",
                review.getId(), "review:" + review.getReviewerId() + ":product:" + review.getProductId(),
                review.getCreatedAt());
    }

    @Override
    @Transactional
    public void recordProductPublished(Product product) {
        if (product == null || product.getStatus() != ProductStatus.PUBLISHED || product.getPublishedAt() == null) return;
        record(product.getSellerId(), LoyaltyEventType.PRODUCT_PUBLISHED, "PRODUCT", product.getId(),
                "product:" + product.getId() + ":published", product.getPublishedAt());
    }

    @Override
    @Transactional
    public void recordBulletinPublished(BulletinPost post) {
        if (post == null || post.getStatus() != BulletinPostStatus.PUBLISHED || post.getPublishedAt() == null) return;
        record(post.getAuthorId(), LoyaltyEventType.BULLETIN_PUBLISHED, "BULLETIN_POST", post.getId(),
                "bulletin:" + post.getId() + ":published", post.getPublishedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public EngagementProfileResponse getProfile(UUID userId) {
        ensureAccount(userId);
        UserProfile profile = profileRepository.readByUserId(userId);
        List<BadgeResponse> badges = badgeRepository.readByUserId(userId).stream()
                .map(BadgeResponse::from).toList();
        return EngagementProfileResponse.of(userId, profile,
                pointRepository.totalPoints(userId, EngagementTrack.BUYER),
                pointRepository.totalPoints(userId, EngagementTrack.SELLER), badges);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoyaltyPointResponse> listPoints(UUID userId, int page, int size) {
        ensureAccount(userId);
        return pointRepository.readByUserId(userId, pageRequest(page, size)).map(LoyaltyPointResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryResponse> leaderboard(EngagementTrack track, int page, int size) {
        if (track == null) throw new ValidationException("A leaderboard track is required.");
        PageRequest pageable = pageRequest(page, size);
        Role role = track == EngagementTrack.BUYER ? Role.BUYER : Role.SELLER;
        Page<LoyaltyLeaderboardProjection> rows = pointRepository.readLeaderboard(
                track, role, AccountStatus.ACTIVE, pageable);
        List<LeaderboardEntryResponse> content = new ArrayList<>();
        for (int index = 0; index < rows.getContent().size(); index++) {
            LoyaltyLeaderboardProjection row = rows.getContent().get(index);
            UserProfile profile = profileRepository.readByUserId(row.getUserId());
            List<BadgeResponse> badges = badgeRepository.readByUserIdAndTrack(row.getUserId(), track)
                    .stream().map(BadgeResponse::from).toList();
            content.add(new LeaderboardEntryResponse(pageable.getOffset() + index + 1, row.getUserId(),
                    profile == null ? "Community member" : profile.getDisplayName(),
                    profile == null ? null : profile.getAvatarStorageKey(), track,
                    row.getPoints() == null ? 0 : row.getPoints(), badges));
        }
        return new PageImpl<>(content, pageable, rows.getTotalElements());
    }

    private void record(UUID userId, LoyaltyEventType eventType, String sourceType,
                        UUID sourceId, String eventKey, Instant occurredAt) {
        if (pointRepository.readByUserIdAndTrackAndEventKey(userId, eventType.getTrack(), eventKey) != null) return;
        LoyaltyPointEntry entry = LoyaltyPointEntryFactory.create(
                userId, eventType, sourceType, sourceId, eventKey, occurredAt);
        if (entry == null) throw new ValidationException("The loyalty event is invalid.");
        pointRepository.create(entry);
        awardEligibleBadges(userId, eventType.getTrack(), occurredAt);
    }

    private void awardEligibleBadges(UUID userId, EngagementTrack track, Instant awardedAt) {
        for (BadgeCode code : BadgeCode.values()) {
            if (code.getTrack() != track || !eligible(userId, code)
                    || badgeRepository.readByUserIdAndTrackAndBadgeCode(userId, track, code) != null) continue;
            BadgeAward award = BadgeAwardFactory.create(userId, code, awardedAt);
            if (award == null) throw new ValidationException("The badge award is invalid.");
            badgeRepository.create(award);
        }
    }

    private boolean eligible(UUID userId, BadgeCode code) {
        return switch (code) {
            case FIRST_PURCHASE -> eventCount(userId, LoyaltyEventType.PURCHASE_COMPLETED) >= 1;
            case LOYAL_BUYER -> eventCount(userId, LoyaltyEventType.PURCHASE_COMPLETED) >= 5;
            case VERIFIED_REVIEWER -> eventCount(userId, LoyaltyEventType.VERIFIED_REVIEW_CREATED) >= 1;
            case COMMUNITY_CONTRIBUTOR -> eventCount(userId, LoyaltyEventType.BULLETIN_PUBLISHED) >= 3;
            case FIRST_LISTING -> eventCount(userId, LoyaltyEventType.PRODUCT_PUBLISHED) >= 1;
            case FIRST_SALE -> eventCount(userId, LoyaltyEventType.SALE_COMPLETED) >= 1;
            case ESTABLISHED_SELLER -> eventCount(userId, LoyaltyEventType.SALE_COMPLETED) >= 10;
        };
    }

    private long eventCount(UUID userId, LoyaltyEventType eventType) {
        return pointRepository.countEvents(userId, eventType.getTrack(), eventType);
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1) throw new ValidationException("Invalid engagement page request.");
        return PageRequest.of(page, Math.min(size, 100));
    }

    private void ensureAccount(UUID userId) {
        if (userId == null || accountRepository.read(userId) == null) {
            throw ResourceNotFoundException.of("Account");
        }
    }
}
