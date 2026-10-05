package com.example.unimarket.service.impl;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.ProductImageFactory;
import com.example.unimarket.repository.IProductImageRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.request.AddProductImageRequest;
import com.example.unimarket.response.ProductImageResponse;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IProductMediaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProductMediaServiceImpl implements IProductMediaService {
    private static final int MAX_IMAGES = 8;
    private final IProductRepository productRepository;
    private final IProductImageRepository imageRepository;
    private final INotificationService notificationService;
    private final IActorRolePolicy actorRolePolicy;

    public ProductMediaServiceImpl(IProductRepository productRepository,
                                   IProductImageRepository imageRepository,
                                   INotificationService notificationService,
                                   IActorRolePolicy actorRolePolicy) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.notificationService = notificationService;
        this.actorRolePolicy = actorRolePolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> list(UUID productId) {
        if (productRepository.readPublicById(productId) == null) throw ResourceNotFoundException.of("Product");
        return imageRepository.readByProductId(productId).stream().map(ProductImageResponse::from).toList();
    }

    @Override
    @Transactional
    public ProductImageResponse add(UUID sellerId, UUID productId, AddProductImageRequest request) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product product = ownedProduct(sellerId, productId);
        List<ProductImage> existing = imageRepository.readByProductId(productId);
        if (existing.size() >= MAX_IMAGES) throw new ValidationException("A product may have at most 8 images.");
        if (existing.stream().anyMatch(value -> value.getDisplayOrder() == request.displayOrder())) {
            throw new ValidationException("Another image already uses that display order.");
        }
        boolean primary = request.primary() || existing.isEmpty();
        if (primary) {
            existing.stream().filter(image -> image.isPrimaryImage()).forEach(value -> {
                value.setPrimaryImage(false);
                imageRepository.update(value);
            });
        }
        ProductImage image = ProductImageFactory.create(productId, request.imageUrl(), request.altText(),
                request.displayOrder(), primary);
        if (image == null) throw new ValidationException("The product image details are invalid.");
        ProductImage saved = imageRepository.create(image);
        notifyImageChange(product, NotificationType.PRODUCT_IMAGE_ADDED,
                "Product image added", "An image was added to your listing \"" + product.getTitle() + "\".");
        return ProductImageResponse.from(saved);
    }

    @Override
    @Transactional
    public void remove(UUID sellerId, UUID productId, UUID imageId) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product product = ownedProduct(sellerId, productId);
        ProductImage image = imageRepository.read(imageId);
        if (image == null || !productId.equals(image.getProductId())) throw ResourceNotFoundException.of("Product image");
        boolean primary = image.isPrimaryImage();
        imageRepository.delete(imageId);
        if (primary) {
            imageRepository.readByProductId(productId).stream()
                    .min(Comparator.comparingInt((ProductImage candidate) -> candidate.getDisplayOrder()))
                    .ifPresent(value -> { value.setPrimaryImage(true); imageRepository.update(value); });
        }
        notifyImageChange(product, NotificationType.PRODUCT_IMAGE_REMOVED,
                "Product image removed", "An image was removed from your listing \"" + product.getTitle() + "\".");
    }

    private void notifyImageChange(Product product, NotificationType type, String title, String message) {
        notificationService.send(product.getSellerId(), type, title, message, "PRODUCT", product.getId(),
                "product:" + product.getId() + ":" + type.name().toLowerCase() + ":" + UUID.randomUUID());
    }

    private Product ownedProduct(UUID sellerId, UUID productId) {
        Product product = productRepository.readByIdForUpdate(productId);
        if (product == null || sellerId == null || !sellerId.equals(product.getSellerId())) {
            throw ResourceNotFoundException.of("Product");
        }
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new ValidationException("Archived product images cannot be changed.");
        }
        return product;
    }
}
