package com.example.unimarket.service;

import com.example.unimarket.request.SimulatePaymentRequest;
import com.example.unimarket.response.PaymentOptionResponse;
import com.example.unimarket.response.SimulatedPaymentResponse;
import java.util.List;
import java.util.UUID;

public interface ISimulatedPaymentService {
    List<PaymentOptionResponse> options();
    SimulatedPaymentResponse simulate(UUID buyerId, String idempotencyKey, SimulatePaymentRequest request);
    List<SimulatedPaymentResponse> list(UUID buyerId);
    SimulatedPaymentResponse get(UUID buyerId, UUID paymentId);
}
