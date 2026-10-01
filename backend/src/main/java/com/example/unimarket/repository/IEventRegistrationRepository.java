package com.example.unimarket.repository;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IEventRegistrationRepository extends IRepository<EventRegistration, UUID> {
    EventRegistration readByEventIdAndAttendeeId(UUID eventId, UUID attendeeId);
    List<EventRegistration> readByEventId(UUID eventId);
    List<EventRegistration> readByAttendeeId(UUID attendeeId);
    EventRegistration readFirstWaitlisted(UUID eventId);
    long countByEventIdAndStatus(UUID eventId, EventRegistrationStatus status);
    List<EventRegistration> readByEventIdAndStatuses(UUID eventId, Collection<EventRegistrationStatus> statuses);
}
