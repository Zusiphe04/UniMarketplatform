package com.example.unimarket.factory;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import com.example.unimarket.util.Helper;

import java.time.Instant;
import java.util.UUID;

public final class EventRegistrationFactory {
    private EventRegistrationFactory() { }

    public static EventRegistration create(UUID eventId, UUID attendeeId, EventRegistrationStatus status, Instant now) {
        if (eventId == null || attendeeId == null || now == null
                || (status != EventRegistrationStatus.CONFIRMED && status != EventRegistrationStatus.WAITLISTED)) {
            return null;
        }
        return new EventRegistration.Builder().setId(Helper.generateId()).setBulletinPostId(eventId)
                .setAttendeeId(attendeeId).setStatus(status).setQueuedAt(now)
                .setConfirmedAt(status == EventRegistrationStatus.CONFIRMED ? now : null).build();
    }
}
