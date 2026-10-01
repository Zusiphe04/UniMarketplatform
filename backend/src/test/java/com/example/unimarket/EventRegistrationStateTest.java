package com.example.unimarket;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventRegistrationStateTest {
    @Test
    void confirmedRegistrationCanCancelAndRejoinWaitlist() {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        EventRegistration registration = new EventRegistration.Builder()
                .setId(UUID.randomUUID()).setBulletinPostId(UUID.randomUUID()).setAttendeeId(UUID.randomUUID())
                .setStatus(EventRegistrationStatus.CONFIRMED).setQueuedAt(now).setConfirmedAt(now).build();

        assertTrue(registration.cancel(now.plusSeconds(60), false));
        assertEquals(EventRegistrationStatus.CANCELLED, registration.getStatus());
        assertTrue(registration.reregister(EventRegistrationStatus.WAITLISTED, now.plusSeconds(120)));
        assertEquals(EventRegistrationStatus.WAITLISTED, registration.getStatus());
        assertTrue(registration.confirm(now.plusSeconds(180)));
        assertEquals(EventRegistrationStatus.CONFIRMED, registration.getStatus());
    }
}
