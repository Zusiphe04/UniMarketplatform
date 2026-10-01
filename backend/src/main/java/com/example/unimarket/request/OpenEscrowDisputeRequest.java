package com.example.unimarket.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpenEscrowDisputeRequest(
        @NotBlank(message = "A dispute reason is required") @Size(max = 1000) String reason
) { }
