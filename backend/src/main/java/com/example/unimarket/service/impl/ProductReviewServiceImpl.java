package com.example.unimarket.service.impl;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductReview;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.ProductReviewFactory;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.repository.IProductReviewRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.CreateReviewRequest;
import com.example.unimarket.request.RespondToReviewRequest;
import com.example.unimarket.request.UpdateReviewRequest;
import com.example.unimarket.response.ProductRatingSummaryResponse;
import com.example.unimarket.response.ProductReviewResponse;
import com.example.unimarket.service.IEngagementService;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IProductReviewService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductReviewServiceImpl implements IProductReviewService {
    private final IProductReviewRepository reviewRepository;
    private final IProductRepository productRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IUserProfileRepository profileRepository;
    private final INotificationService notificationService;
    private final IEngagementService engagementService;

    public ProductReviewServiceImpl(IProductReviewRepository reviewRepository,
                                    IProductRepository productRepository,
                                    IOrderItemRepository orderItemRepository,
                                    IUserProfileRepository profileRepository,
                                    INotificationService notificationService,
                                    IEngagementService engagementService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
        this.engagementService = engagementService;
    }

    @Override @Transactional(readOnly = true)
    public Page<ProductReviewResponse> list(UUID productId, int page, int size) {
        ensureProduct(productId);
        if (page < 0 || size < 1) throw new ValidationException("Invalid review page request.");
        return reviewRepository.readByProductId(productId, PageRequest.of(page, Math.min(size, 100)))
                .map(review -> ProductReviewResponse.from(review, displayName(review.getReviewerId())));
    }

    @Override @Transactional(readOnly = true)
    public ProductRatingSummaryResponse summary(UUID productId) {
        ensureProduct(productId);
        return ProductRatingSummaryResponse.of(productId, reviewRepository.averageRating(productId),
                reviewRepository.countByProductId(productId));
    }

    @Override @Transactional
    public ProductReviewResponse create(UUID reviewerId, CreateReviewRequest request) {
        ensureProduct(request.productId());
        if (!orderItemRepository.existsPaidPurchase(reviewerId, request.productId())) {
            throw new ValidationException("Only a verified buyer of this product can review it.");
        }
        if (reviewRepository.readByProductIdAndReviewerId(request.productId(), reviewerId) != null) {
            throw new ValidationException("You have already reviewed this product.");
        }
        ProductReview review = ProductReviewFactory.create(
                request.productId(), reviewerId, request.rating(), request.comment());
        if (review == null) throw new ValidationException("The review is invalid.");
        ProductReview saved = reviewRepository.create(review);
        engagementService.recordReviewCreated(saved);
        return ProductReviewResponse.from(saved, displayName(reviewerId));
    }

    @Override @Transactional
    public ProductReviewResponse update(UUID reviewerId, UUID reviewId, UpdateReviewRequest request) {
        ProductReview review = ownedReview(reviewerId, reviewId);
        ProductReview updated = ProductReviewFactory.update(review, request.rating(), request.comment());
        if (updated == null) throw new ValidationException("The review is invalid.");
        return ProductReviewResponse.from(reviewRepository.update(updated), displayName(reviewerId));
    }

    @Override @Transactional
    public ProductReviewResponse respond(UUID sellerId, UUID reviewId, RespondToReviewRequest request) {
        ProductReview review = reviewRepository.read(reviewId);
        Product product = review == null ? null : productRepository.read(review.getProductId());
        if (review == null || product == null || !product.getSellerId().equals(sellerId)) {
            throw ResourceNotFoundException.of("Review");
        }
        ProductReview responded = ProductReviewFactory.respond(review, request.response());
        if (responded == null) {
            throw new ValidationException("This review already has a seller response or the response is invalid.");
        }
        ProductReview saved = reviewRepository.update(responded);
        notificationService.send(
                saved.getReviewerId(),
                NotificationType.REVIEW_RESPONSE,
                "Seller responded to your review",
                "The seller responded to your review of " + product.getTitle() + ".",
                "PRODUCT_REVIEW",
                saved.getId(),
                "review:" + saved.getId() + ":response");
        return ProductReviewResponse.from(saved, displayName(saved.getReviewerId()));
    }

    @Override @Transactional
    public void delete(UUID reviewerId, UUID reviewId) {
        ProductReview review = ownedReview(reviewerId, reviewId);
        reviewRepository.delete(review.getId());
    }

    @Override @Transactional
    public void moderateDelete(UUID reviewId) {
        if (!reviewRepository.delete(reviewId)) throw ResourceNotFoundException.of("Review");
    }

    private ProductReview ownedReview(UUID reviewerId, UUID reviewId) {
        ProductReview review = reviewRepository.read(reviewId);
        if (review == null || !review.getReviewerId().equals(reviewerId)) {
            throw ResourceNotFoundException.of("Review");
        }
        return review;
    }
    private void ensureProduct(UUID id) {
        if (productRepository.read(id) == null) throw ResourceNotFoundException.of("Product");
    }
    private String displayName(UUID userId) {
        UserProfile profile = profileRepository.readByUserId(userId);
        return profile == null ? "Community member" : profile.getDisplayName();
    }
}
