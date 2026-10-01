package com.example.unimarket.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RespondToReviewRequest(
        @NotBlank(message = "Seller response is required") @Size(max = 1000) String response
) { }
