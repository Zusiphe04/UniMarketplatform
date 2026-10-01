package com.example.unimarket.response;

/**
 * Internal carrier for a freshly minted access token and its lifetime.
 *
 * <p>Kept separate from {@link AuthResponse} so the token service can return
 * the token and its expiry without needing to know about account summaries.
 */
public record AccessTokenResponse(String token, long expiresInSeconds) {
}
