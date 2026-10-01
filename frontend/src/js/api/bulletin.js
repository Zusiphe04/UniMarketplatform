import { ApiError, apiRequest } from './client.js';

function numericValue(value, fallback) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

export async function fetchCommunityEvents({ page = 0, size = 9, signal } = {}) {
  const params = new URLSearchParams({
    type: 'EVENT',
    page: String(page),
    size: String(size),
  });
  const payload = await apiRequest(`/api/v1/bulletin?${params}`, { signal });

  if (!payload || !Array.isArray(payload.content)) {
    throw new ApiError('The Community events service returned an unexpected response.');
  }

  return {
    content: payload.content,
    number: numericValue(payload.number, page),
    size: numericValue(payload.size, size),
    totalElements: numericValue(payload.totalElements, 0),
    totalPages: numericValue(payload.totalPages, 0),
  };
}
