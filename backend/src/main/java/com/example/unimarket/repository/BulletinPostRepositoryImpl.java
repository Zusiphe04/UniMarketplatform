package com.example.unimarket.repository;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class BulletinPostRepositoryImpl implements IBulletinPostRepository {
    private final BulletinPostJpaRepository jpaRepository;
    public BulletinPostRepositoryImpl(BulletinPostJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    @Override public BulletinPost create(BulletinPost value) { return value == null ? null : jpaRepository.save(value); }
    @Override public BulletinPost read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public BulletinPost readByIdForUpdate(UUID id) { return id == null ? null : jpaRepository.findByIdForUpdate(id).orElse(null); }
    @Override public BulletinPost update(BulletinPost value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId()) ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<BulletinPost> getAll() { return jpaRepository.findAll(); }
    @Override public Page<BulletinPost> readPublished(Instant now, Pageable pageable) {
        return jpaRepository.findPublished(BulletinPostStatus.PUBLISHED, now, pageable);
    }
    @Override public Page<BulletinPost> readPublishedByType(BulletinPostType type, Instant now, Pageable pageable) {
        return jpaRepository.findPublishedByType(BulletinPostStatus.PUBLISHED, type, now, pageable);
    }
    @Override public Page<BulletinPost> readByAuthorId(UUID authorId, Pageable pageable) {
        return jpaRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }
}
