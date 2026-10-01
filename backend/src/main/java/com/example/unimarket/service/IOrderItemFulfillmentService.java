package com.example.unimarket.service;

import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.request.UpdateOrderItemFulfillmentRequest;
import com.example.unimarket.response.OrderItemFulfillmentResponse;

import java.util.List;
import java.util.UUID;

public interface IOrderItemFulfillmentService {
    void initializeForPaidOrder(MarketplaceOrder order, List<OrderItem> items);
    List<OrderItemFulfillmentResponse> listForBuyer(UUID buyerId, UUID orderId);
    List<OrderItemFulfillmentResponse> listForSeller(UUID sellerId, UUID orderId);
    OrderItemFulfillmentResponse updateBySeller(UUID sellerId, UUID orderId, UUID orderItemId,
                                                UpdateOrderItemFulfillmentRequest request);
    OrderItemFulfillmentResponse confirmReceipt(UUID buyerId, UUID orderId, UUID orderItemId);
}
