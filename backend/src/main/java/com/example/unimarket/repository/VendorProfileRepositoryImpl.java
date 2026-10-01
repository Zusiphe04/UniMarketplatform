package com.example.unimarket.repository;

import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.VendorVerificationStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class VendorProfileRepositoryImpl implements IVendorProfileRepository {
    private static volatile VendorProfileRepositoryImpl instance;
    private final VendorProfileJpaRepository jpaRepository;

    public VendorProfileRepositoryImpl(VendorProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        if (instance == null) instance = this;
    }

    public static VendorProfileRepositoryImpl getInstance(VendorProfileJpaRepository repository) {
        if (instance == null) {
            synchronized (VendorProfileRepositoryImpl.class) {
                if (instance == null) instance = new VendorProfileRepositoryImpl(repository);
            }
        }
        return instance;
    }

    @Override public VendorProfile create(VendorProfile value) { return value == null ? null : jpaRepository.save(value); }
    @Override public VendorProfile read(UUID id) { return id == null ? null : jpaRepository.findById(id).orElse(null); }
    @Override public VendorProfile update(VendorProfile value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id); return true;
    }
    @Override public List<VendorProfile> getAll() { return jpaRepository.findAll(); }
    @Override public VendorProfile readByUserId(UUID userId) {
        return userId == null ? null : jpaRepository.findByUserId(userId).orElse(null);
    }
    @Override public List<VendorProfile> readByStatus(VendorVerificationStatus status) {
        return status == null ? getAll() : jpaRepository.findByVerificationStatusOrderBySubmittedAtAsc(status);
    }
}
