package com.example.unimarket.request;

import com.example.unimarket.domain.enums.FulfillmentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotNull(message = "Fulfillment method is required") FulfillmentMethod fulfillmentMethod,
        @Size(max = 500) String deliveryAddress
) { }
