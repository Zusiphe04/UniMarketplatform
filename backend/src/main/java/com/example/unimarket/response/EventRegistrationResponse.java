package com.example.unimarket.response;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public record EventRegistrationResponse(
        UUID id, UUID eventId, UUID attendeeId, EventRegistrationStatus status,
        Instant queuedAt, Instant confirmedAt, Instant cancelledAt,
        Integer capacity, long confirmedAttendeeCount, Integer remainingCapacity, Integer waitlistPosition
) {
    public static EventRegistrationResponse from(EventRegistration registration, Integer capacity,
                                                 long confirmedCount, Integer waitlistPosition) {
        Integer remaining = capacity == null ? null : Math.max(0, capacity - Math.toIntExact(confirmedCount));
        return new EventRegistrationResponse(registration.getId(), registration.getBulletinPostId(),
                registration.getAttendeeId(), registration.getStatus(), registration.getQueuedAt(),
                registration.getConfirmedAt(), registration.getCancelledAt(), capacity, confirmedCount,
                remaining, waitlistPosition);
    }
}
