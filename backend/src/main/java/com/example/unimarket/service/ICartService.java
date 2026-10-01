package com.example.unimarket.service;

import com.example.unimarket.request.AddCartItemRequest;
import com.example.unimarket.request.UpdateCartItemRequest;
import com.example.unimarket.response.CartResponse;

import java.util.UUID;

public interface ICartService {
    CartResponse getCart(UUID buyerId);
    CartResponse addItem(UUID buyerId, AddCartItemRequest request);
    CartResponse updateItem(UUID buyerId, UUID productId, UpdateCartItemRequest request);
    CartResponse removeItem(UUID buyerId, UUID productId);
    void clear(UUID buyerId);
}
