package com.example.unimarket.controller;

import com.example.unimarket.request.ChangePasswordRequest;
import com.example.unimarket.request.LoginRequest;
import com.example.unimarket.request.RegisterRequest;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.response.AuthResponse;
import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.RegistrationResponse;
import com.example.unimarket.service.IAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/** Ecommerce-style registration and JWT authentication endpoints. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IAuthService authService;
    private final String refreshCookieName;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthController(
            IAuthService authService,
            @Value("${unimarket.security.refresh-cookie-name}") String refreshCookieName,
            @Value("${unimarket.security.refresh-cookie-secure}") boolean refreshCookieSecure,
            @Value("${unimarket.security.refresh-cookie-same-site}") String refreshCookieSameSite) {
        this.authService = authService;
        this.refreshCookieName = refreshCookieName;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        IAuthService.LoginOutcome outcome = authService.login(request, sessionContext(httpRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(outcome).toString())
                .body(outcome.authResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "${unimarket.security.refresh-cookie-name}", required = false)
            String refreshToken,
            HttpServletRequest httpRequest) {
        IAuthService.LoginOutcome outcome = authService.refresh(refreshToken, sessionContext(httpRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(outcome).toString())
                .body(outcome.authResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = "${unimarket.security.refresh-cookie-name}", required = false)
            String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .body(MessageResponse.of("Signed out."));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        authService.logoutAll(currentUserId(jwt));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .body(MessageResponse.of("Signed out of all devices."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserId(jwt), request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .body(MessageResponse.of("Password updated. Please sign in again."));
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> currentAccount(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getCurrentAccount(currentUserId(jwt)));
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        return UUID.fromString(jwt.getSubject());
    }

    private IAuthService.SessionContext sessionContext(HttpServletRequest request) {
        return new IAuthService.SessionContext(clientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    private ResponseCookie refreshCookie(IAuthService.LoginOutcome outcome) {
        return baseCookie(outcome.rawRefreshToken())
                .maxAge(Duration.ofSeconds(outcome.refreshTokenMaxAgeSeconds())).build();
    }

    private ResponseCookie expiredRefreshCookie() {
        return baseCookie("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(refreshCookieName, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth");
    }
}
