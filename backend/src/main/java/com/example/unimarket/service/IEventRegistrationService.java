package com.example.unimarket.service;

import com.example.unimarket.response.EventAttendeeResponse;
import com.example.unimarket.response.EventRegistrationResponse;

import java.util.List;
import java.util.UUID;

public interface IEventRegistrationService {
    EventRegistrationResponse register(UUID attendeeId, UUID eventId);
    EventRegistrationResponse getOwnRegistration(UUID attendeeId, UUID eventId);
    EventRegistrationResponse cancelOwnRegistration(UUID attendeeId, UUID eventId);
    List<EventRegistrationResponse> listOwnRegistrations(UUID attendeeId);
    List<EventAttendeeResponse> listHostedAttendees(UUID hostId, UUID eventId);
    void cancelHostedEvent(UUID hostId, UUID eventId, String reason);
}
