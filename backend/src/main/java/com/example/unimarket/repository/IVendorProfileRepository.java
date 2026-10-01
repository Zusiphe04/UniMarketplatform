package com.example.unimarket.repository;

import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.VendorVerificationStatus;

import java.util.List;
import java.util.UUID;

public interface IVendorProfileRepository extends IRepository<VendorProfile, UUID> {
    VendorProfile readByUserId(UUID userId);
    List<VendorProfile> readByStatus(VendorVerificationStatus status);
}
