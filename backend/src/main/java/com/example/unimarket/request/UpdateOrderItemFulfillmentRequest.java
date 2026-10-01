package com.example.unimarket.request;

import com.example.unimarket.domain.enums.OrderItemFulfillmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderItemFulfillmentRequest(
        @NotNull(message = "Fulfillment state is required") OrderItemFulfillmentStatus status,
        @Size(max = 500) String pickupInstructions,
        @Size(max = 160) String trackingReference
) { }
