package com.example.unimarket.request;

import com.example.unimarket.domain.enums.PaymentOption;
import com.example.unimarket.domain.enums.PaymentScenario;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Contains no card, CVV, bank, password, or banking OTP fields. */
public record SimulatePaymentRequest(
        @NotNull(message = "Order id is required") UUID orderId,
        @NotNull(message = "Payment option is required") PaymentOption paymentOption,
        @NotNull(message = "Payment scenario is required") PaymentScenario scenario
) { }
