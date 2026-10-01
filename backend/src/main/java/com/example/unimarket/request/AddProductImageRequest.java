package com.example.unimarket.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProductImageRequest(
        @NotBlank(message = "Image URL is required") @Size(max = 1000) String imageUrl,
        @Size(max = 200) String altText,
        @Min(0) @Max(7) int displayOrder,
        boolean primary
) { }
