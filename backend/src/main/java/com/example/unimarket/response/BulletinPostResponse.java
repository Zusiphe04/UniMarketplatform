package com.example.unimarket.response;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;

import java.time.Instant;
import java.util.UUID;

public record BulletinPostResponse(
        UUID id, UUID authorId, String authorDisplayName, BulletinPostType type, BulletinPostStatus status,
        String title, String content, String location, String coverImageUrl, Instant eventStartsAt,
        Integer eventCapacity, long confirmedAttendeeCount, Integer remainingCapacity, boolean eventCancelled,
        Instant eventCancelledAt, String eventCancellationReason, Instant expiresAt, Instant publishedAt,
        Instant createdAt, Instant updatedAt
) {
    public static BulletinPostResponse from(BulletinPost post, String authorDisplayName, long confirmedAttendeeCount) {
        Integer capacity = post.getEventCapacity();
        Integer remaining = capacity == null ? null : Math.max(0, capacity - Math.toIntExact(confirmedAttendeeCount));
        return new BulletinPostResponse(post.getId(), post.getAuthorId(), authorDisplayName,
                post.getType(), post.getStatus(), post.getTitle(), post.getContent(), post.getLocation(),
                post.getCoverImageUrl(), post.getEventStartsAt(), capacity, confirmedAttendeeCount, remaining,
                post.isEventCancelled(), post.getEventCancelledAt(), post.getEventCancellationReason(),
                post.getExpiresAt(), post.getPublishedAt(), post.getCreatedAt(), post.getUpdatedAt());
    }
}
