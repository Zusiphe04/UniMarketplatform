package com.example.unimarket.service.impl;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.BulletinPostFactory;
import com.example.unimarket.repository.IBulletinPostRepository;
import com.example.unimarket.repository.IEventRegistrationRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.CreateBulletinPostRequest;
import com.example.unimarket.request.UpdateBulletinPostRequest;
import com.example.unimarket.response.BulletinPostResponse;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.IBulletinPostService;
import com.example.unimarket.service.IEngagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BulletinPostServiceImpl implements IBulletinPostService {
    private final IBulletinPostRepository bulletinRepository;
    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;
    private final IEventRegistrationRepository registrationRepository;
    private final IEngagementService engagementService;
    private final IActorRolePolicy actorRolePolicy;

    public BulletinPostServiceImpl(IBulletinPostRepository bulletinRepository,
                                   IUserAccountRepository accountRepository,
                                   IUserProfileRepository profileRepository,
                                   IEventRegistrationRepository registrationRepository,
                                   IEngagementService engagementService,
                                   IActorRolePolicy actorRolePolicy) {
        this.bulletinRepository = bulletinRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.registrationRepository = registrationRepository;
        this.engagementService = engagementService;
        this.actorRolePolicy = actorRolePolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulletinPostResponse> listPublished(BulletinPostType type, int page, int size) {
        PageRequest pageable = pageRequest(page, size);
        Page<BulletinPost> posts = type == null
                ? bulletinRepository.readPublished(Instant.now(), pageable)
                : bulletinRepository.readPublishedByType(type, Instant.now(), pageable);
        return posts.map(this::response);
    }

    @Override
    @Transactional(readOnly = true)
    public BulletinPostResponse getPublished(UUID postId) {
        BulletinPost post = bulletinRepository.read(postId);
        Instant now = Instant.now();
        if (post == null || post.getStatus() != BulletinPostStatus.PUBLISHED
                || post.getExpiresAt() != null && !post.getExpiresAt().isAfter(now)) {
            throw ResourceNotFoundException.of("Bulletin post");
        }
        return response(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulletinPostResponse> listOwn(UUID authorId, int page, int size) {
        actorRolePolicy.requireBuyerOnly(authorId);
        return bulletinRepository.readByAuthorId(authorId, pageRequest(page, size)).map(this::response);
    }

    @Override
    @Transactional
    public BulletinPostResponse create(UUID authorId, CreateBulletinPostRequest request) {
        actorRolePolicy.requireBuyerOnly(authorId);
        ensureActive(authorId);
        BulletinPost post = BulletinPostFactory.create(authorId, request.type(), request.title(), request.content(),
                request.location(), request.coverImageUrl(), request.eventStartsAt(), request.eventCapacity(),
                request.expiresAt(), request.publishNow());
        if (post == null) {
            throw new ValidationException("The bulletin post is invalid. Events require a future start time, and capacity must be positive when supplied.");
        }
        BulletinPost saved = bulletinRepository.create(post);
        if (saved.getStatus() == BulletinPostStatus.PUBLISHED) engagementService.recordBulletinPublished(saved);
        return response(saved);
    }

    @Override
    @Transactional
    public BulletinPostResponse update(UUID authorId, UUID postId, UpdateBulletinPostRequest request) {
        actorRolePolicy.requireBuyerOnly(authorId);
        BulletinPost post = owned(authorId, postId);
        if (post.getType() == BulletinPostType.EVENT && post.getEventCapacity() != null
                && request.eventCapacity() != null
                && request.eventCapacity() < registrationRepository.countByEventIdAndStatus(postId, EventRegistrationStatus.CONFIRMED)) {
            throw new ValidationException("Event capacity cannot be lower than the number of confirmed attendees.");
        }
        BulletinPost updated = BulletinPostFactory.update(post, request.type(), request.title(), request.content(),
                request.location(), request.coverImageUrl(), request.eventStartsAt(), request.eventCapacity(),
                request.expiresAt());
        if (updated == null) {
            throw new ValidationException("Only an active draft or published post with valid details can be updated.");
        }
        return response(bulletinRepository.update(updated));
    }

    @Override
    @Transactional
    public BulletinPostResponse publish(UUID authorId, UUID postId) {
        actorRolePolicy.requireBuyerOnly(authorId);
        BulletinPost post = BulletinPostFactory.publish(owned(authorId, postId));
        if (post == null) throw new ValidationException("Only an unexpired draft can be published.");
        BulletinPost saved = bulletinRepository.update(post);
        engagementService.recordBulletinPublished(saved);
        return response(saved);
    }

    @Override
    @Transactional
    public BulletinPostResponse archive(UUID authorId, UUID postId) {
        actorRolePolicy.requireBuyerOnly(authorId);
        BulletinPost post = BulletinPostFactory.archive(owned(authorId, postId));
        if (post == null) throw new ValidationException("This post cannot be archived.");
        return response(bulletinRepository.update(post));
    }

    @Override
    @Transactional
    public void moderateRemove(UUID moderatorId, UUID postId) {
        BulletinPost post = bulletinRepository.read(postId);
        if (post == null) throw ResourceNotFoundException.of("Bulletin post");
        if (BulletinPostFactory.remove(post, moderatorId) == null) {
            throw new ValidationException("This bulletin post has already been removed.");
        }
        bulletinRepository.update(post);
    }

    private BulletinPost owned(UUID authorId, UUID postId) {
        BulletinPost post = bulletinRepository.read(postId);
        if (post == null || !post.getAuthorId().equals(authorId)) throw ResourceNotFoundException.of("Bulletin post");
        return post;
    }

    private void ensureActive(UUID authorId) {
        UserAccount account = accountRepository.read(authorId);
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can create bulletin posts.");
        }
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1) throw new ValidationException("Invalid bulletin page request.");
        return PageRequest.of(page, Math.min(size, 100));
    }

    private BulletinPostResponse response(BulletinPost post) {
        UserProfile profile = profileRepository.readByUserId(post.getAuthorId());
        String displayName = profile == null ? "Community member" : profile.getDisplayName();
        long confirmed = post.getType() == BulletinPostType.EVENT
                ? registrationRepository.countByEventIdAndStatus(post.getId(), EventRegistrationStatus.CONFIRMED) : 0;
        return BulletinPostResponse.from(post, displayName, confirmed);
    }
}
