package com.example.unimarket.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelEventRequest(
        @NotBlank(message = "A cancellation reason is required") @Size(max = 500) String reason
) { }
