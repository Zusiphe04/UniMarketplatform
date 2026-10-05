package com.example.unimarket.service.impl;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.ProductCategory;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.ProductFactory;
import com.example.unimarket.repository.IProductImageRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.CreateProductRequest;
import com.example.unimarket.request.UpdateProductRequest;
import com.example.unimarket.response.ProductResponse;
import com.example.unimarket.response.SellerSummaryResponse;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.IEngagementService;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {
    private static final int MAX_PAGE_SIZE = 100;
    private final IProductRepository productRepository;
    private final IProductImageRepository imageRepository;
    private final IUserProfileRepository profileRepository;
    private final IEngagementService engagementService;
    private final INotificationService notificationService;
    private final IActorRolePolicy actorRolePolicy;

    public ProductServiceImpl(IProductRepository productRepository, IProductImageRepository imageRepository,
                              IUserProfileRepository profileRepository, IEngagementService engagementService,
                              INotificationService notificationService, IActorRolePolicy actorRolePolicy) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.profileRepository = profileRepository;
        this.engagementService = engagementService;
        this.notificationService = notificationService;
        this.actorRolePolicy = actorRolePolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchPublic(int page, int size, String query, ListingCategory category) {
        validatePage(page, size);
        String normalized = query == null || query.isBlank() ? null : query.trim();
        Collection<ProductCategory> persistenceCategories = category == null
                ? List.of(ProductCategory.values()) : category.persistenceCategories();
        Page<Product> products = productRepository.searchPublic(
                normalized, persistenceCategories, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)));
        return responses(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getPublicProduct(UUID productId) {
        Product product = productRepository.readPublicById(productId);
        if (product == null) throw ResourceNotFoundException.of("Product");
        return response(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listOwnedProducts(UUID sellerId, int page, int size) {
        actorRolePolicy.requireSellerOnly(sellerId);
        validatePage(page, size);
        return responses(productRepository.readBySellerId(
                sellerId, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE))));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(UUID sellerId, CreateProductRequest request) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product product = ProductFactory.create(sellerId, request.title(), request.description(),
                request.category(), request.condition(), request.price(), request.quantity(),
                request.brand(), request.model(), request.storage(), request.memory(), request.processor(),
                request.screenSize(), request.color(), request.size());
        if (product == null) throw new ValidationException("The product details or category-specific specifications are not valid.");
        return response(productRepository.create(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID sellerId, UUID productId, UpdateProductRequest request) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product product = readOwnedForUpdate(sellerId, productId);
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new ValidationException("Archived products cannot be updated.");
        }
        if (product.getReservedQuantity() > 0) {
            throw new ValidationException("This product has reserved stock and cannot be updated until checkout is completed or expires.");
        }
        Product updated = ProductFactory.update(product, request.title(), request.description(),
                request.category(), request.condition(), request.price(), request.quantity(),
                request.brand(), request.model(), request.storage(), request.memory(), request.processor(),
                request.screenSize(), request.color(), request.size());
        if (updated == null) {
            throw new ValidationException("The product details or category-specific specifications are not valid.");
        }
        Product saved = productRepository.update(updated);
        notifyProduct(saved, NotificationType.PRODUCT_UPDATED, "Product listing updated",
                "Your listing \"" + saved.getTitle() + "\" was updated successfully.");
        return response(saved);
    }

    @Override
    @Transactional
    public ProductResponse publishProduct(UUID sellerId, UUID productId) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product published = ProductFactory.publish(readOwnedForUpdate(sellerId, productId));
        if (published == null) throw new ValidationException("Only a draft product with stock can be published.");
        Product saved = productRepository.update(published);
        engagementService.recordProductPublished(saved);
        notifyProduct(saved, NotificationType.PRODUCT_PUBLISHED, "Product listing published",
                "Your listing \"" + saved.getTitle() + "\" is now visible in the marketplace.");
        return response(saved);
    }

    @Override
    @Transactional
    public ProductResponse archiveProduct(UUID sellerId, UUID productId) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Product archived = ProductFactory.archive(readOwnedForUpdate(sellerId, productId));
        if (archived == null) throw new ValidationException("Only a draft, published, or sold-out product can be archived.");
        Product saved = productRepository.update(archived);
        notifyProduct(saved, NotificationType.PRODUCT_ARCHIVED, "Product listing archived",
                "Your listing \"" + saved.getTitle() + "\" was removed from the marketplace.");
        return response(saved);
    }

    private Product readOwnedForUpdate(UUID sellerId, UUID productId) {
        Product product = productRepository.readByIdForUpdate(productId);
        if (product == null || sellerId == null || !sellerId.equals(product.getSellerId())) {
            throw ResourceNotFoundException.of("Product");
        }
        return product;
    }

    private Page<ProductResponse> responses(Page<Product> products) {
        Map<UUID, List<ProductImage>> media = imageRepository
                .readByProductIds(products.getContent().stream().map(Product::getId).toList())
                .stream().collect(Collectors.groupingBy(ProductImage::getProductId));
        Map<UUID, UserProfile> profiles = profileRepository.readByUserIds(products.getContent().stream()
                        .map(Product::getSellerId).distinct().toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, value -> value));
        return products.map(product -> ProductResponse.from(product,
                media.getOrDefault(product.getId(), List.of()), seller(product, profiles.get(product.getSellerId()))));
    }

    private ProductResponse response(Product product) {
        return ProductResponse.from(product, imageRepository.readByProductId(product.getId()),
                seller(product, profileRepository.readByUserId(product.getSellerId())));
    }

    private SellerSummaryResponse seller(Product product, UserProfile profile) {
        return SellerSummaryResponse.from(product.getSellerId(), profile);
    }

    private void notifyProduct(Product product, NotificationType type, String title, String message) {
        notificationService.send(product.getSellerId(), type, title, message, "PRODUCT", product.getId(),
                "product:" + product.getId() + ":" + type.name().toLowerCase() + ":" + UUID.randomUUID());
    }

    private void validatePage(int page, int size) {
        if (page < 0) throw new ValidationException("Page must not be negative.");
        if (size < 1) throw new ValidationException("Page size must be at least 1.");
    }
}
