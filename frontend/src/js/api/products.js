import { ApiError, apiRequest } from './client.js';

function numericValue(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

export async function fetchProducts({ page = 0, size = 8, query = '', category = '', signal } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (query.trim()) params.set('query', query.trim());
  if (category) params.set('category', category);

  const payload = await apiRequest(`/api/v1/products?${params}`, { auth: false, signal }, false);
  if (!payload || !Array.isArray(payload.content)) {
    throw new ApiError('The catalogue returned an unexpected response.');
  }

  return {
    content: payload.content,
    number: numericValue(payload.number, 0),
    size: numericValue(payload.size, size),
    totalElements: numericValue(payload.totalElements, 0),
    totalPages: numericValue(payload.totalPages, 0),
  };
}
