package com.example.unimarket.request;

import com.example.unimarket.domain.enums.EscrowResolution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveEscrowRequest(
        @NotNull EscrowResolution resolution,
        @NotBlank(message = "A resolution note is required") @Size(max = 1000) String note
) { }
