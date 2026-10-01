package com.example.unimarket.service;

import com.example.unimarket.domain.enums.VendorVerificationStatus;
import com.example.unimarket.request.ReviewVendorProfileRequest;
import com.example.unimarket.request.SubmitVendorProfileRequest;
import com.example.unimarket.response.VendorProfileResponse;

import java.util.List;
import java.util.UUID;

public interface IVendorProfileService {
    VendorProfileResponse getOwn(UUID userId);
    VendorProfileResponse submit(UUID userId, SubmitVendorProfileRequest request);
    List<VendorProfileResponse> listForReview(VendorVerificationStatus status);
    VendorProfileResponse review(UUID userId, ReviewVendorProfileRequest request, UUID adminId);
}
