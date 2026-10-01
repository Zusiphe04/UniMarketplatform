package com.example.unimarket.service;

import com.example.unimarket.request.CreateReviewRequest;
import com.example.unimarket.request.RespondToReviewRequest;
import com.example.unimarket.request.UpdateReviewRequest;
import com.example.unimarket.response.ProductRatingSummaryResponse;
import com.example.unimarket.response.ProductReviewResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IProductReviewService {
    Page<ProductReviewResponse> list(UUID productId, int page, int size);
    ProductRatingSummaryResponse summary(UUID productId);
    ProductReviewResponse create(UUID reviewerId, CreateReviewRequest request);
    ProductReviewResponse update(UUID reviewerId, UUID reviewId, UpdateReviewRequest request);
    ProductReviewResponse respond(UUID sellerId, UUID reviewId, RespondToReviewRequest request);
    void delete(UUID reviewerId, UUID reviewId);
    void moderateDelete(UUID reviewId);
}
