package com.example.unimarket.controller;

import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.request.GrantRoleRequest;
import com.example.unimarket.request.UpdateAccountStatusRequest;
import com.example.unimarket.response.AccountResponse;
import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.PersonaResponse;
import com.example.unimarket.service.IRoleService;
import com.example.unimarket.service.IUserAccountService;
import com.example.unimarket.service.IUserPersonaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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

/**
 * Administrative account and role management.
 *
 * <p>Exposes the repository's read, update, delete, and getAll operations behind
 * an authorised service. The whole controller requires the {@code ADMIN} role:
 * the filter chain already restricts {@code /api/v1/admin/**}, and the
 * annotation repeats the requirement so the rule survives a future change to
 * the URL layout.
 */
@RestController
@RequestMapping("/api/v1/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final IUserAccountService accountService;
    private final IRoleService roleService;
    private final IUserPersonaService personaService;

    public AdminAccountController(IUserAccountService accountService,
                                  IRoleService roleService,
                                  IUserPersonaService personaService) {
        this.accountService = accountService;
        this.roleService = roleService;
        this.personaService = personaService;
    }

    /** Repository {@code getAll}. */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    /** Repository {@code read}. */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }

    /** Repository {@code update}, restricted to the status field. */
    @PatchMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateStatus(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable UUID accountId,
                                                        @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(accountService.updateStatus(accountId, request, currentUserId(jwt)));
    }

    /** Closes an account while retaining rows required by audit and commerce history. */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<MessageResponse> deleteAccount(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable UUID accountId) {
        boolean closed = accountService.deleteAccount(accountId, currentUserId(jwt));

        return closed
                ? ResponseEntity.ok(MessageResponse.of("Account closed."))
                : ResponseEntity.status(404).body(MessageResponse.of("No account with that id."));
    }

    @PostMapping("/{accountId}/roles")
    public ResponseEntity<MessageResponse> grantRole(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable UUID accountId,
                                                     @Valid @RequestBody GrantRoleRequest request) {

        boolean granted = roleService.grant(accountId, request.role(), currentUserId(jwt));

        return granted
                ? ResponseEntity.ok(MessageResponse.of("Role " + request.role() + " granted."))
                : ResponseEntity.ok(MessageResponse.of("Account already holds " + request.role() + "."));
    }

    @DeleteMapping("/{accountId}/roles/{role}")
    public ResponseEntity<MessageResponse> revokeRole(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable UUID accountId,
                                                      @PathVariable Role role) {

        boolean revoked = roleService.revoke(accountId, role, currentUserId(jwt));

        return revoked
                ? ResponseEntity.ok(MessageResponse.of("Role " + role + " revoked."))
                : ResponseEntity.status(404).body(MessageResponse.of("Account does not hold " + role + "."));
    }

    @PostMapping("/{accountId}/personas/{persona}/verify")
    public ResponseEntity<PersonaResponse> verifyPersona(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable UUID accountId,
                                                         @PathVariable CommunityPersona persona) {
        return ResponseEntity.ok(personaService.verify(accountId, persona, currentUserId(jwt)));
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }
        return UUID.fromString(jwt.getSubject());
    }
}
