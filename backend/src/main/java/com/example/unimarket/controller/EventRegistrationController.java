package com.example.unimarket.controller;

import com.example.unimarket.request.CancelEventRequest;
import com.example.unimarket.response.EventAttendeeResponse;
import com.example.unimarket.response.EventRegistrationResponse;
import com.example.unimarket.service.IEventRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@PreAuthorize("isAuthenticated()")
public class EventRegistrationController {
    private final IEventRegistrationService eventService;
    public EventRegistrationController(IEventRegistrationService eventService) { this.eventService = eventService; }

    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<EventRegistrationResponse> register(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.register(userId(jwt), eventId));
    }

    @GetMapping("/{eventId}/registrations/me")
    public ResponseEntity<EventRegistrationResponse> mine(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getOwnRegistration(userId(jwt), eventId));
    }

    @DeleteMapping("/{eventId}/registrations/me")
    public ResponseEntity<EventRegistrationResponse> cancelRegistration(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.cancelOwnRegistration(userId(jwt), eventId));
    }

    @GetMapping("/registrations/mine")
    public ResponseEntity<List<EventRegistrationResponse>> listMine(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(eventService.listOwnRegistrations(userId(jwt)));
    }

    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<List<EventAttendeeResponse>> attendees(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.listHostedAttendees(userId(jwt), eventId));
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId,
                                            @Valid @RequestBody CancelEventRequest request) {
        eventService.cancelHostedEvent(userId(jwt), eventId, request.reason());
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
