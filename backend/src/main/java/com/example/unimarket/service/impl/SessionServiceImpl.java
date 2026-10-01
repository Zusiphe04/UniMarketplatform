package com.example.unimarket.service.impl;

import com.example.unimarket.domain.AuthSession;
import com.example.unimarket.domain.enums.RevocationReason;
import com.example.unimarket.factory.AuthSessionFactory;
import com.example.unimarket.repository.IAuthSessionRepository;
import com.example.unimarket.response.SessionResponse;
import com.example.unimarket.service.ISessionService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Session review and revocation for the authenticated member.
 */
@Service
public class SessionServiceImpl implements ISessionService {

    private final IAuthSessionRepository sessionRepository;

    public SessionServiceImpl(IAuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> listActiveSessions(UUID userId) {
        return sessionRepository.readActiveByUserId(userId).stream()
                .map(SessionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public boolean revokeSession(UUID userId, UUID sessionId) {
        AuthSession session = sessionRepository.read(sessionId);
        if (session == null) {
            return false;
        }

        // Ownership is checked here rather than trusting the path variable.
        // Holding a valid token must not allow revoking someone else's session.
        if (!session.getUserId().equals(userId)) {
            throw new AccessDeniedException("This session belongs to another account.");
        }

        if (session.isRevoked()) {
            return false;
        }

        AuthSession revoked = AuthSessionFactory.revoke(session, RevocationReason.LOGOUT);
        return revoked != null && sessionRepository.update(revoked) != null;
    }
}
