package com.example.unimarket.factory;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.util.Helper;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public final class BulletinPostFactory {
    private BulletinPostFactory() { }

    public static BulletinPost create(UUID authorId, BulletinPostType type, String title, String content,
                                      String location, String coverImageUrl, Instant eventStartsAt,
                                      Integer eventCapacity, Instant expiresAt, boolean publishNow) {
        String cleanTitle = Helper.cleanText(title);
        String cleanContent = cleanBody(content);
        String cleanLocation = optionalText(location);
        String cleanCoverImageUrl = optionalText(coverImageUrl);
        Instant now = Instant.now();
        if (!valid(authorId, type, cleanTitle, cleanContent, cleanLocation, cleanCoverImageUrl,
                eventStartsAt, eventCapacity, expiresAt, now)) return null;
        return new BulletinPost.Builder()
                .setId(Helper.generateId()).setAuthorId(authorId).setType(type)
                .setStatus(publishNow ? BulletinPostStatus.PUBLISHED : BulletinPostStatus.DRAFT)
                .setTitle(cleanTitle).setContent(cleanContent).setLocation(cleanLocation)
                .setCoverImageUrl(cleanCoverImageUrl).setEventStartsAt(eventStartsAt)
                .setEventCapacity(eventCapacity).setExpiresAt(expiresAt)
                .setPublishedAt(publishNow ? now : null).build();
    }

    public static BulletinPost update(BulletinPost post, BulletinPostType type, String title, String content,
                                      String location, String coverImageUrl, Instant eventStartsAt,
                                      Integer eventCapacity, Instant expiresAt) {
        String cleanTitle = Helper.cleanText(title);
        String cleanContent = cleanBody(content);
        String cleanLocation = optionalText(location);
        String cleanCoverImageUrl = optionalText(coverImageUrl);
        return post != null && valid(post.getAuthorId(), type, cleanTitle, cleanContent, cleanLocation,
                cleanCoverImageUrl, eventStartsAt, eventCapacity, expiresAt, Instant.now())
                && post.update(type, cleanTitle, cleanContent, cleanLocation, cleanCoverImageUrl,
                eventStartsAt, eventCapacity, expiresAt) ? post : null;
    }

    public static BulletinPost publish(BulletinPost post) {
        if (post == null) return null;
        Instant now = Instant.now();
        if (post.getExpiresAt() != null && !post.getExpiresAt().isAfter(now)) return null;
        return post.publish(now) ? post : null;
    }

    public static BulletinPost cancelEvent(BulletinPost post, String reason) {
        String cleanReason = Helper.cleanText(reason);
        return post != null && cleanReason != null && cleanReason.length() <= 500
                && post.cancelEvent(cleanReason, Instant.now()) ? post : null;
    }

    public static BulletinPost archive(BulletinPost post) {
        return post != null && post.archive() ? post : null;
    }

    public static BulletinPost remove(BulletinPost post, UUID moderatorId) {
        return post != null && post.remove(moderatorId, Instant.now()) ? post : null;
    }

    private static boolean valid(UUID authorId, BulletinPostType type, String title, String content,
                                 String location, String coverImageUrl, Instant eventStartsAt,
                                 Integer eventCapacity, Instant expiresAt, Instant now) {
        boolean event = type == BulletinPostType.EVENT;
        return authorId != null && type != null && title != null && title.length() <= 160
                && content != null && content.length() <= 5000
                && (location == null || location.length() <= 200)
                && (coverImageUrl == null || coverImageUrl.length() <= 1000 && validUrl(coverImageUrl))
                && (event || coverImageUrl == null)
                && (!event || eventStartsAt != null && eventStartsAt.isAfter(now))
                && (!event || eventCapacity == null || eventCapacity > 0)
                && (event || eventCapacity == null)
                && (expiresAt == null || expiresAt.isAfter(now));
    }

    private static boolean validUrl(String value) {
        try {
            URI uri = URI.create(value);
            return uri.getHost() != null && ("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static String cleanBody(String value) { return Helper.isNullOrEmpty(value) ? null : value.trim(); }
    private static String optionalText(String value) { return Helper.isNullOrEmpty(value) ? null : Helper.cleanText(value); }
}
