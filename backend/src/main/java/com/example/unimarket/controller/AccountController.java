package com.example.unimarket.controller;

import com.example.unimarket.request.DeclarePersonaRequest;
import com.example.unimarket.request.UpdateProfileRequest;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.response.PersonaResponse;
import com.example.unimarket.response.UserProfileResponse;
import com.example.unimarket.service.IAuthService;
import com.example.unimarket.service.IUserPersonaService;
import com.example.unimarket.service.IUserProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * The authenticated member's own account and profile.
 *
 * <p>Every route derives the account identifier from the JWT subject. No
 * endpoint accepts an account id from the client, which removes the possibility
 * of reading or editing another member's data by changing a path variable.
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final IAuthService authService;
    private final IUserProfileService profileService;
    private final IUserPersonaService personaService;

    public AccountController(IAuthService authService,
                             IUserProfileService profileService,
                             IUserPersonaService personaService) {
        this.authService = authService;
        this.profileService = profileService;
        this.personaService = personaService;
    }

    @GetMapping
    public ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getCurrentAccount(currentUserId(jwt)));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getByUserId(currentUserId(jwt)));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(currentUserId(jwt), request));
    }

    @GetMapping("/personas")
    public ResponseEntity<List<PersonaResponse>> getPersonas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(personaService.getPersonas(currentUserId(jwt)));
    }

    @PostMapping("/personas")
    public ResponseEntity<PersonaResponse> declarePersona(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody DeclarePersonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personaService.declare(currentUserId(jwt), request));
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
