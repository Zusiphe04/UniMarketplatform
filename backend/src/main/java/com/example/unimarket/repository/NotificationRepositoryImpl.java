package com.example.unimarket.repository;

import com.example.unimarket.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {
    private final NotificationJpaRepository jpaRepository;
    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public Notification create(Notification value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public Notification read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public Notification update(Notification value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<Notification> getAll() { return jpaRepository.findAll(); }
    @Override public Page<Notification> readByRecipientId(UUID recipientId, Pageable pageable) {
        return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }
    @Override public Notification readByRecipientIdAndId(UUID recipientId, UUID id) {
        return jpaRepository.findByRecipientIdAndId(recipientId, id).orElse(null);
    }
    @Override public Notification readByRecipientIdAndEventKey(UUID recipientId, String eventKey) {
        return jpaRepository.findByRecipientIdAndEventKey(recipientId, eventKey).orElse(null);
    }
    @Override public long countUnread(UUID recipientId) {
        return jpaRepository.countByRecipientIdAndReadAtIsNull(recipientId);
    }
    @Override public int markAllRead(UUID recipientId, Instant readAt) {
        return jpaRepository.markAllRead(recipientId, readAt);
    }
}
