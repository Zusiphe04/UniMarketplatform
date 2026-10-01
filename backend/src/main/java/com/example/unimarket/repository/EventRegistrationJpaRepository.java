package com.example.unimarket.repository;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationJpaRepository extends JpaRepository<EventRegistration, UUID> {
    Optional<EventRegistration> findByBulletinPostIdAndAttendeeId(UUID bulletinPostId, UUID attendeeId);
    List<EventRegistration> findByBulletinPostIdOrderByQueuedAtAscIdAsc(UUID bulletinPostId);
    List<EventRegistration> findByAttendeeIdOrderByCreatedAtDesc(UUID attendeeId);
    Optional<EventRegistration> findFirstByBulletinPostIdAndStatusOrderByQueuedAtAscIdAsc(
            UUID bulletinPostId, EventRegistrationStatus status);
    long countByBulletinPostIdAndStatus(UUID bulletinPostId, EventRegistrationStatus status);
    List<EventRegistration> findByBulletinPostIdAndStatusInOrderByQueuedAtAscIdAsc(
            UUID bulletinPostId, Collection<EventRegistrationStatus> statuses);
}
