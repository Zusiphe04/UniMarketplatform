package com.example.unimarket.repository;

import com.example.unimarket.domain.MarketplaceOrder;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public class MarketplaceOrderRepositoryImpl implements IMarketplaceOrderRepository {
    private final MarketplaceOrderJpaRepository jpaRepository;
    public MarketplaceOrderRepositoryImpl(MarketplaceOrderJpaRepository repository) { jpaRepository = repository; }
    @Override public MarketplaceOrder create(MarketplaceOrder value) { return value == null ? null : jpaRepository.save(value); }
    @Override public MarketplaceOrder read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public MarketplaceOrder update(MarketplaceOrder value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<MarketplaceOrder> getAll() { return jpaRepository.findAll(); }
    @Override public MarketplaceOrder readByIdForUpdate(UUID id) {
        return id == null ? null : jpaRepository.findByIdForUpdate(id).orElse(null);
    }
    @Override public MarketplaceOrder readByBuyerIdAndIdempotencyKey(UUID buyerId, String key) {
        return buyerId == null || key == null ? null
                : jpaRepository.findByBuyerIdAndIdempotencyKey(buyerId, key).orElse(null);
    }
    @Override public List<MarketplaceOrder> readByBuyerId(UUID id) {
        return id == null ? List.of() : jpaRepository.findByBuyerIdOrderByPlacedAtDesc(id);
    }
}
