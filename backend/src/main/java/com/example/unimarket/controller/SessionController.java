package com.example.unimarket.controller;

import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.SessionResponse;
import com.example.unimarket.service.ISessionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Lets a member see where they are signed in and sign a device out.
 *
 * <p>Session identifiers are accepted in the path, so the service verifies
 * ownership before revoking anything.
 */
@RestController
@RequestMapping("/api/v1/account/sessions")
public class SessionController {

    private final ISessionService sessionService;

    public SessionController(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> listSessions(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(sessionService.listActiveSessions(currentUserId(jwt)));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<MessageResponse> revokeSession(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable UUID sessionId) {
        boolean revoked = sessionService.revokeSession(currentUserId(jwt), sessionId);

        return revoked
                ? ResponseEntity.ok(MessageResponse.of("Session revoked."))
                : ResponseEntity.status(404).body(MessageResponse.of("No active session with that id."));
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
