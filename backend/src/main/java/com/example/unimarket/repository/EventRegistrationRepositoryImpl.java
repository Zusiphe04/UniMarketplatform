package com.example.unimarket.repository;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class EventRegistrationRepositoryImpl implements IEventRegistrationRepository {
    private final EventRegistrationJpaRepository jpaRepository;
    public EventRegistrationRepositoryImpl(EventRegistrationJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }
    @Override public EventRegistration create(EventRegistration value) { return value == null ? null : jpaRepository.save(value); }
    @Override public EventRegistration read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public EventRegistration update(EventRegistration value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId()) ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<EventRegistration> getAll() { return jpaRepository.findAll(); }
    @Override public EventRegistration readByEventIdAndAttendeeId(UUID eventId, UUID attendeeId) {
        return eventId == null || attendeeId == null ? null : jpaRepository.findByBulletinPostIdAndAttendeeId(eventId, attendeeId).orElse(null);
    }
    @Override public List<EventRegistration> readByEventId(UUID eventId) {
        return eventId == null ? List.of() : jpaRepository.findByBulletinPostIdOrderByQueuedAtAscIdAsc(eventId);
    }
    @Override public List<EventRegistration> readByAttendeeId(UUID attendeeId) {
        return attendeeId == null ? List.of() : jpaRepository.findByAttendeeIdOrderByCreatedAtDesc(attendeeId);
    }
    @Override public EventRegistration readFirstWaitlisted(UUID eventId) {
        return eventId == null ? null : jpaRepository.findFirstByBulletinPostIdAndStatusOrderByQueuedAtAscIdAsc(
                eventId, EventRegistrationStatus.WAITLISTED).orElse(null);
    }
    @Override public long countByEventIdAndStatus(UUID eventId, EventRegistrationStatus status) {
        return eventId == null || status == null ? 0 : jpaRepository.countByBulletinPostIdAndStatus(eventId, status);
    }
    @Override public List<EventRegistration> readByEventIdAndStatuses(UUID eventId, Collection<EventRegistrationStatus> statuses) {
        return eventId == null || statuses == null || statuses.isEmpty() ? List.of()
                : jpaRepository.findByBulletinPostIdAndStatusInOrderByQueuedAtAscIdAsc(eventId, statuses);
    }
}
