package com.example.unimarket.request;

import com.example.unimarket.domain.enums.ListingCategory;
import com.example.unimarket.domain.enums.ProductCondition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Complete editable-field replacement for a draft product. */
public record UpdateProductRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must not exceed 160 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 4000, message = "Description must not exceed 4000 characters")
        String description,

        @NotNull(message = "Category is required")
        ListingCategory category,

        @NotNull(message = "Condition is required")
        ProductCondition condition,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        @Digits(integer = 17, fraction = 2, message = "Price must have at most 17 integer and 2 decimal digits")
        BigDecimal price,

        @Min(value = 0, message = "Quantity must not be negative")
        int quantity,

        @Size(max = 80, message = "Brand must not exceed 80 characters")
        String brand,

        @Size(max = 120, message = "Model must not exceed 120 characters")
        String model,

        @Size(max = 80, message = "Storage must not exceed 80 characters")
        String storage,

        @Size(max = 80, message = "Memory must not exceed 80 characters")
        String memory,

        @Size(max = 120, message = "Processor must not exceed 120 characters")
        String processor,

        @Size(max = 80, message = "Screen size must not exceed 80 characters")
        String screenSize,

        @Size(max = 80, message = "Color must not exceed 80 characters")
        String color,

        @Size(max = 80, message = "Size must not exceed 80 characters")
        String size
) {
}
