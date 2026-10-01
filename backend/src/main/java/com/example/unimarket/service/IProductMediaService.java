package com.example.unimarket.service;

import com.example.unimarket.request.AddProductImageRequest;
import com.example.unimarket.response.ProductImageResponse;

import java.util.List;
import java.util.UUID;

public interface IProductMediaService {
    List<ProductImageResponse> list(UUID productId);
    ProductImageResponse add(UUID sellerId, UUID productId, AddProductImageRequest request);
    void remove(UUID sellerId, UUID productId, UUID imageId);
}
