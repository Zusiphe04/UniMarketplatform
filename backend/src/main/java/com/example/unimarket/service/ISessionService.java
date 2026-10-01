package com.example.unimarket.service;

import com.example.unimarket.response.SessionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lets a member review and revoke their own sessions.
 */
public interface ISessionService {

    /**
     * Lists the caller's active sessions.
     */
    List<SessionResponse> listActiveSessions(UUID userId);

    /**
     * Revokes one of the caller's sessions.
     *
     * @throws org.springframework.security.access.AccessDeniedException when the
     *         session belongs to another account
     * @return {@code true} when an active session was revoked
     */
    boolean revokeSession(UUID userId, UUID sessionId);
}
