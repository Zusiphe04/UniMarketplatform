package com.example.unimarket.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReviewRequest(
        @NotNull(message = "Product id is required") UUID productId,
        @Min(1) @Max(5) int rating,
        @NotBlank(message = "Review comment is required") @Size(max = 1000) String comment
) { }
