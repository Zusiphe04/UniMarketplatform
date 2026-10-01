package com.example.unimarket.request;

import com.example.unimarket.domain.enums.VendorVerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewVendorProfileRequest(
        @NotNull(message = "Review status is required") VendorVerificationStatus status,
        @Size(max = 500) String reviewNote
) { }
