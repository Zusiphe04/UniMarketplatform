package com.example.unimarket.service;

import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.request.CreateProductRequest;
import com.example.unimarket.request.UpdateProductRequest;
import com.example.unimarket.response.ProductResponse;

import org.springframework.data.domain.Page;

import java.util.UUID;

/** Product catalogue discovery and seller lifecycle operations. */
public interface IProductService {

    Page<ProductResponse> searchPublic(int page, int size, String query, ListingCategory category);

    ProductResponse getPublicProduct(UUID productId);

    Page<ProductResponse> listOwnedProducts(UUID sellerId, int page, int size);

    ProductResponse createProduct(UUID sellerId, CreateProductRequest request);

    ProductResponse updateProduct(UUID sellerId, UUID productId, UpdateProductRequest request);

    ProductResponse publishProduct(UUID sellerId, UUID productId);

    ProductResponse archiveProduct(UUID sellerId, UUID productId);
}
