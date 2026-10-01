package com.example.unimarket.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "Product id is required") UUID productId,
        @Min(1) @Max(99) int quantity
) { }
