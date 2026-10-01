package com.example.unimarket.repository;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.enums.BulletinPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface IBulletinPostRepository extends IRepository<BulletinPost, UUID> {
    BulletinPost readByIdForUpdate(UUID id);
    Page<BulletinPost> readPublished(Instant now, Pageable pageable);
    Page<BulletinPost> readPublishedByType(BulletinPostType type, Instant now, Pageable pageable);
    Page<BulletinPost> readByAuthorId(UUID authorId, Pageable pageable);
}
