package com.example.unimarket.repository;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BulletinPostJpaRepository extends JpaRepository<BulletinPost, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BulletinPost b where b.id = :id")
    Optional<BulletinPost> findByIdForUpdate(@Param("id") UUID id);

    @Query("select b from BulletinPost b where b.status = :status and (b.expiresAt is null or b.expiresAt > :now) order by b.publishedAt desc, b.createdAt desc")
    Page<BulletinPost> findPublished(@Param("status") BulletinPostStatus status, @Param("now") Instant now, Pageable pageable);

    @Query("select b from BulletinPost b where b.status = :status and b.type = :type and (b.expiresAt is null or b.expiresAt > :now) order by b.publishedAt desc, b.createdAt desc")
    Page<BulletinPost> findPublishedByType(@Param("status") BulletinPostStatus status,
                                           @Param("type") BulletinPostType type,
                                           @Param("now") Instant now, Pageable pageable);

    Page<BulletinPost> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);
}
