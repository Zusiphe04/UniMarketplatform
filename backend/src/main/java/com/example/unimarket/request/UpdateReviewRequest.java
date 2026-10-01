package com.example.unimarket.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Min(1) @Max(5) int rating,
        @NotBlank(message = "Review comment is required") @Size(max = 1000) String comment
) { }
