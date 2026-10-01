package com.example.unimarket.request;

import com.example.unimarket.domain.enums.AccountStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for an administrator changing an account's status.
 *
 * <p>A reason is mandatory, so a suspension is always accountable.
 */
public record UpdateAccountStatusRequest(

        @NotNull(message = "Status is required")
        AccountStatus status,

        @NotBlank(message = "A reason is required")
        @Size(max = 500)
        String reason
) {
}
