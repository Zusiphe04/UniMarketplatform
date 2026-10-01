package com.example.unimarket.request;

import com.example.unimarket.domain.enums.VendorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitVendorProfileRequest(
        @NotNull(message = "Vendor type is required") VendorType vendorType,
        @NotBlank(message = "Business or trading name is required")
        @Size(max = 160) String businessName,
        @NotBlank(message = "Description is required")
        @Size(max = 1000) String description,
        @Size(max = 80) String registrationNumber,
        @Size(max = 500) String websiteUrl
) { }
