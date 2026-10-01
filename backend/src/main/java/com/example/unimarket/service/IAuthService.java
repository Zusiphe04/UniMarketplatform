package com.example.unimarket.service;

import com.example.unimarket.request.ChangePasswordRequest;
import com.example.unimarket.request.LoginRequest;
import com.example.unimarket.request.RegisterRequest;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.response.AuthResponse;
import com.example.unimarket.response.RegistrationResponse;

import java.util.UUID;

/** Application service for registration, JWT authentication, and credentials. */
public interface IAuthService {

    /** Creates an immediately active account using the Ecommerce-style flow. */
    RegistrationResponse register(RegisterRequest request);

    /** Authenticates a member and starts a refresh-token session. */
    LoginOutcome login(LoginRequest request, SessionContext sessionContext);

    /** Rotates a refresh token and issues a new JWT access token. */
    LoginOutcome refresh(String rawRefreshToken, SessionContext sessionContext);

    /** Revokes the session represented by the supplied refresh token. */
    void logout(String rawRefreshToken);

    /** Revokes every active session for an account. */
    void logoutAll(UUID userId);

    /** Changes an authenticated member's password and revokes existing sessions. */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /** Returns the authenticated account, roles, and community personas. */
    AccountResponse getCurrentAccount(UUID userId);

    record LoginOutcome(AuthResponse authResponse, String rawRefreshToken,
                        long refreshTokenMaxAgeSeconds) { }

    record SessionContext(String ipAddress, String userAgent) { }
}
