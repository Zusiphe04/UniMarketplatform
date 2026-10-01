package com.example.unimarket.repository;

import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.VendorVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorProfileJpaRepository extends JpaRepository<VendorProfile, UUID> {
    Optional<VendorProfile> findByUserId(UUID userId);
    List<VendorProfile> findByVerificationStatusOrderBySubmittedAtAsc(VendorVerificationStatus status);
}
