package com.example.unimarket.service;

import com.example.unimarket.request.CheckoutRequest;
import com.example.unimarket.response.OrderResponse;
import com.example.unimarket.response.SellerOrderResponse;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    OrderResponse checkout(UUID buyerId, String idempotencyKey, CheckoutRequest request);
    List<OrderResponse> listBuyerOrders(UUID buyerId);
    OrderResponse getBuyerOrder(UUID buyerId, UUID orderId);
    OrderResponse cancelBuyerOrder(UUID buyerId, UUID orderId);
    List<SellerOrderResponse> listSellerOrders(UUID sellerId);
}
