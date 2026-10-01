package com.example.unimarket.repository;

import com.example.unimarket.domain.CartItem;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ICartItemRepository extends IRepository<CartItem, UUID> {
    List<CartItem> readByBuyerId(UUID buyerId);
    List<CartItem> readByBuyerIdForUpdate(UUID buyerId);
    CartItem readByBuyerIdAndProductId(UUID buyerId, UUID productId);
    int deleteByIds(Collection<UUID> ids);
    int deleteByBuyerId(UUID buyerId);
}
