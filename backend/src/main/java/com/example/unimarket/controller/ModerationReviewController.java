package com.example.unimarket.controller;

import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.service.IProductReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation/reviews")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationReviewController {
    private final IProductReviewService reviewService;

    public ModerationReviewController(IProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID reviewId) {
        reviewService.moderateDelete(reviewId);
        return ResponseEntity.ok(MessageResponse.of("Review removed by moderation."));
    }
}
