package com.example.unimarket.service.impl;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.ProductImage;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.domain.enums.ProductCategory;
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
import com.example.unimarket.service.IEngagementService;
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

    public ProductServiceImpl(IProductRepository productRepository, IProductImageRepository imageRepository,
                              IUserProfileRepository profileRepository, IEngagementService engagementService) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.profileRepository = profileRepository;
        this.engagementService = engagementService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchPublic(int page, int size, String query, ListingCategory category) {
        if (page < 0) throw new ValidationException("Page must not be negative.");
        if (size < 1) throw new ValidationException("Page size must be at least 1.");
        String normalized = query == null || query.isBlank() ? null : query.trim();
        Collection<ProductCategory> persistenceCategories = category == null
                ? List.of(ProductCategory.values()) : category.persistenceCategories();
        Page<Product> products = productRepository.searchPublic(
                normalized, persistenceCategories, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)));
        Map<UUID, List<ProductImage>> media = imageRepository
                .readByProductIds(products.getContent().stream().map(Product::getId).toList())
                .stream().collect(Collectors.groupingBy(ProductImage::getProductId));
        Map<UUID, UserProfile> profiles = profileRepository.readByUserIds(products.getContent().stream()
                        .map(Product::getSellerId).distinct().toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, value -> value));
        return products.map(product -> ProductResponse.from(product,
                media.getOrDefault(product.getId(), List.of()), seller(product, profiles.get(product.getSellerId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getPublicProduct(UUID productId) {
        Product product = productRepository.readPublicById(productId);
        if (product == null) throw ResourceNotFoundException.of("Product");
        return response(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(UUID sellerId, CreateProductRequest request) {
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
        Product product = readOwnedForUpdate(sellerId, productId);
        Product updated = ProductFactory.update(product, request.title(), request.description(),
                request.category(), request.condition(), request.price(), request.quantity(),
                request.brand(), request.model(), request.storage(), request.memory(), request.processor(),
                request.screenSize(), request.color(), request.size());
        if (updated == null) throw new ValidationException("Only a draft product with valid category-specific details can be updated.");
        return response(productRepository.update(updated));
    }

    @Override
    @Transactional
    public ProductResponse publishProduct(UUID sellerId, UUID productId) {
        Product published = ProductFactory.publish(readOwnedForUpdate(sellerId, productId));
        if (published == null) throw new ValidationException("Only a draft product with stock can be published.");
        Product saved = productRepository.update(published);
        engagementService.recordProductPublished(saved);
        return response(saved);
    }

    @Override
    @Transactional
    public ProductResponse archiveProduct(UUID sellerId, UUID productId) {
        Product archived = ProductFactory.archive(readOwnedForUpdate(sellerId, productId));
        if (archived == null) throw new ValidationException("Only a draft, published, or sold-out product can be archived.");
        return response(productRepository.update(archived));
    }

    private Product readOwnedForUpdate(UUID sellerId, UUID productId) {
        Product product = productRepository.readByIdForUpdate(productId);
        if (product == null || sellerId == null || !sellerId.equals(product.getSellerId())) throw ResourceNotFoundException.of("Product");
        return product;
    }

    private ProductResponse response(Product product) {
        return ProductResponse.from(product, imageRepository.readByProductId(product.getId()),
                seller(product, profileRepository.readByUserId(product.getSellerId())));
    }

    private SellerSummaryResponse seller(Product product, UserProfile profile) {
        return SellerSummaryResponse.from(product.getSellerId(), profile);
    }
}
