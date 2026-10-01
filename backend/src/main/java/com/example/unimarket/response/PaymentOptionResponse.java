package com.example.unimarket.response;

import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentOptionCategory;

public record PaymentOptionResponse(
        PaymentOption code,
        String displayName,
        PaymentOptionCategory category,
        String description
) {
    public static PaymentOptionResponse from(PaymentOption option) {
        return new PaymentOptionResponse(option, option.getDisplayName(),
                option.getCategory(), option.getDescription());
    }
}
