package com.example.unimarket.response;

import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.EventRegistrationStatus;

import java.time.Instant;
import java.util.UUID;

public record EventAttendeeResponse(UUID registrationId, UUID attendeeId, String displayName,
                                    EventRegistrationStatus status, Instant queuedAt,
                                    Instant confirmedAt, Integer waitlistPosition) {
    public static EventAttendeeResponse from(EventRegistration registration, UserProfile profile,
                                             Integer waitlistPosition) {
        String name = profile == null || profile.getDisplayName() == null ? "Community member" : profile.getDisplayName();
        return new EventAttendeeResponse(registration.getId(), registration.getAttendeeId(), name,
                registration.getStatus(), registration.getQueuedAt(), registration.getConfirmedAt(), waitlistPosition);
    }
}
