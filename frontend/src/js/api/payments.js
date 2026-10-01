import { ApiError, apiRequest } from './client.js';

export async function fetchPaymentOptions({ signal } = {}) {
  const payload = await apiRequest('/api/v1/payments/options', { signal });
  if (!Array.isArray(payload) || payload.some((option) => !option?.code || !option?.displayName)) {
    throw new ApiError('The simulated payment options returned an unexpected response.');
  }
  if (payload.length === 0) throw new ApiError('No simulated payment options are currently available.');
  return payload;
}

export function simulatePayment({ orderId, paymentOption, scenario, idempotencyKey }) {
  return apiRequest('/api/v1/payments/simulate', {
    method: 'POST',
    headers: { 'Idempotency-Key': idempotencyKey },
    body: { orderId, paymentOption, scenario },
  });
}
