package com.example.unimarket.response;

/**
 * Returned after a successful login or refresh.
 *
 * <p>Carries only the access token. The refresh token is delivered separately
 * in a {@code Secure} {@code HttpOnly} cookie and never appears in a response
 * body, so JavaScript on the page cannot read it.
 *
 * @param accessToken the signed JWT
 * @param tokenType   always {@code Bearer}
 * @param expiresIn   access-token lifetime in seconds
 * @param account     safe summary of the authenticated member
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AccountResponse account
) {

    private static final String BEARER = "Bearer";

    public static AuthResponse of(String accessToken, long expiresIn, AccountResponse account) {
        return new AuthResponse(accessToken, BEARER, expiresIn, account);
    }
}
