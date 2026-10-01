package com.example.unimarket.controller;

import com.example.unimarket.request.AddProductImageRequest;
import com.example.unimarket.request.CreateProductRequest;
import com.example.unimarket.request.UpdateProductRequest;
import com.example.unimarket.response.ImageUploadResponse;
import com.example.unimarket.response.MessageResponse;
import com.example.unimarket.response.ProductImageResponse;
import com.example.unimarket.response.ProductResponse;
import com.example.unimarket.service.IMediaStorageService;
import com.example.unimarket.service.IProductMediaService;
import com.example.unimarket.service.IProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/products")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {
    private final IProductService productService;
    private final IProductMediaService mediaService;
    private final IMediaStorageService mediaStorageService;

    public SellerProductController(IProductService productService, IProductMediaService mediaService,
                                   IMediaStorageService mediaStorageService) {
        this.productService = productService;
        this.mediaService = mediaService;
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(sellerId(jwt), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(sellerId(jwt), id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ProductResponse> publishProduct(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return ResponseEntity.ok(productService.publishProduct(sellerId(jwt), id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ProductResponse> archiveProduct(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return ResponseEntity.ok(productService.archiveProduct(sellerId(jwt), id));
    }

    @PostMapping(path = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(@AuthenticationPrincipal Jwt jwt,
            @RequestPart("image") MultipartFile image) {
        sellerId(jwt);
        String path = mediaStorageService.storeImage(image, "products");
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(imageUrl));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImageResponse> addImage(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id, @Valid @RequestBody AddProductImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.add(sellerId(jwt), id, request));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<MessageResponse> removeImage(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id, @PathVariable UUID imageId) {
        mediaService.remove(sellerId(jwt), id, imageId);
        return ResponseEntity.ok(MessageResponse.of("Product image removed."));
    }

    private UUID sellerId(Jwt jwt) {
        if (jwt == null) throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        return UUID.fromString(jwt.getSubject());
    }
}
