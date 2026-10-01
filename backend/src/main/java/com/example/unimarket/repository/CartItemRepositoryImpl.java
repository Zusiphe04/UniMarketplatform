package com.example.unimarket.repository;

import com.example.unimarket.domain.CartItem;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class CartItemRepositoryImpl implements ICartItemRepository {
    private final CartItemJpaRepository jpaRepository;
    public CartItemRepositoryImpl(CartItemJpaRepository repository) { jpaRepository = repository; }
    @Override public CartItem create(CartItem value) { return value == null ? null : jpaRepository.save(value); }
    @Override public CartItem read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public CartItem update(CartItem value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<CartItem> getAll() { return jpaRepository.findAll(); }
    @Override public List<CartItem> readByBuyerId(UUID id) {
        return id == null ? List.of() : jpaRepository.findByBuyerIdOrderByAddedAtAsc(id);
    }
    @Override public List<CartItem> readByBuyerIdForUpdate(UUID id) {
        return id == null ? List.of() : jpaRepository.findByBuyerIdOrderByAddedAtAscForUpdate(id);
    }
    @Override public CartItem readByBuyerIdAndProductId(UUID buyerId, UUID productId) {
        return buyerId == null || productId == null ? null
                : jpaRepository.findByBuyerIdAndProductId(buyerId, productId).orElse(null);
    }
    @Override @Transactional public int deleteByIds(Collection<UUID> ids) {
        return ids == null || ids.isEmpty() ? 0 : jpaRepository.deleteAllByIdIn(ids);
    }
    @Override @Transactional public int deleteByBuyerId(UUID id) {
        return id == null ? 0 : jpaRepository.deleteAllByBuyerId(id);
    }
}
