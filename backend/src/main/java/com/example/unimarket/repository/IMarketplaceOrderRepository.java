package com.example.unimarket.repository;

import com.example.unimarket.domain.MarketplaceOrder;
import java.util.List;
import java.util.UUID;

public interface IMarketplaceOrderRepository extends IRepository<MarketplaceOrder, UUID> {
    MarketplaceOrder readByIdForUpdate(UUID id);
    MarketplaceOrder readByBuyerIdAndIdempotencyKey(UUID buyerId, String idempotencyKey);
    List<MarketplaceOrder> readByBuyerId(UUID buyerId);
}
