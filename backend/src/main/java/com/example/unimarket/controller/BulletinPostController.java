package com.example.unimarket.controller;

import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.request.CreateBulletinPostRequest;
import com.example.unimarket.request.UpdateBulletinPostRequest;
import com.example.unimarket.response.BulletinPostResponse;
import com.example.unimarket.response.ImageUploadResponse;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.IBulletinPostService;
import com.example.unimarket.service.IMediaStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bulletin")
@PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
public class BulletinPostController {
    private final IBulletinPostService bulletinService;
    private final IMediaStorageService mediaStorageService;
    private final IActorRolePolicy actorRolePolicy;

    public BulletinPostController(IBulletinPostService bulletinService, IMediaStorageService mediaStorageService,
                                  IActorRolePolicy actorRolePolicy) {
        this.bulletinService = bulletinService;
        this.mediaStorageService = mediaStorageService;
        this.actorRolePolicy = actorRolePolicy;
    }

    @GetMapping
    public ResponseEntity<Page<BulletinPostResponse>> list(
            @RequestParam(required = false) BulletinPostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bulletinService.listPublished(type, page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<BulletinPostResponse> get(@PathVariable UUID postId) {
        return ResponseEntity.ok(bulletinService.getPublished(postId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Page<BulletinPostResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bulletinService.listOwn(userId(jwt), page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<BulletinPostResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBulletinPostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bulletinService.create(userId(jwt), request));
    }

    @PostMapping(path = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ImageUploadResponse> uploadImage(@AuthenticationPrincipal Jwt jwt,
            @RequestPart("image") MultipartFile image) {
        UUID actorId = userId(jwt);
        actorRolePolicy.requireBuyerOnly(actorId);
        String path = mediaStorageService.storeImage(image, "events");
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(imageUrl));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<BulletinPostResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdateBulletinPostRequest request) {
        return ResponseEntity.ok(bulletinService.update(userId(jwt), postId, request));
    }

    @PatchMapping("/{postId}/publish")
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<BulletinPostResponse> publish(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        return ResponseEntity.ok(bulletinService.publish(userId(jwt), postId));
    }

    @PatchMapping("/{postId}/archive")
    @PreAuthorize("hasRole('BUYER') and !hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<BulletinPostResponse> archive(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        return ResponseEntity.ok(bulletinService.archive(userId(jwt), postId));
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null) throw new AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
