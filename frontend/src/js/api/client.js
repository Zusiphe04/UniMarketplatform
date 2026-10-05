const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
export const API_BASE_URL = configuredBaseUrl ? configuredBaseUrl.replace(/\/+$/, '') : '';

let accessToken = null;
let refreshInFlight = null;

export class ApiError extends Error {
  constructor(message, status = 0, fieldErrors = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

export function setAccessToken(token) {
  accessToken = token || null;
}

export function getAccessToken() {
  return accessToken;
}

async function parseResponse(response) {
  if (response.status === 204) return null;
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('json')) return response.json();
  const text = await response.text();
  return text ? { message: text } : null;
}

async function toApiError(response) {
  let body = null;
  try {
    body = await parseResponse(response);
  } catch {
    // Preserve the HTTP status when an invalid error body is returned.
  }
  return new ApiError(
    body?.detail || body?.message || body?.title || `Request failed with status ${response.status}.`,
    response.status,
    body?.errors || {},
  );
}

async function send(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (accessToken && options.auth !== false) headers.set('Authorization', `Bearer ${accessToken}`);

  let body = options.body;
  if (body !== undefined && body !== null && !(body instanceof FormData) && typeof body !== 'string') {
    headers.set('Content-Type', 'application/json');
    body = JSON.stringify(body);
  }
  if (!headers.has('Accept')) headers.set('Accept', 'application/json');

  return fetch(`${API_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers,
    body,
    credentials: 'include',
    signal: options.signal,
  });
}

export async function refreshSession() {
  if (refreshInFlight) return refreshInFlight;

  refreshInFlight = (async () => {
    const response = await send('/api/v1/auth/refresh', { method: 'POST', auth: false });
    if (!response.ok) {
      setAccessToken(null);
      throw await toApiError(response);
    }
    const payload = await parseResponse(response);
    setAccessToken(payload.accessToken);
    window.dispatchEvent(new CustomEvent('unimarket:session-refreshed', { detail: payload.account }));
    return payload;
  })().finally(() => {
    refreshInFlight = null;
  });

  return refreshInFlight;
}

export async function apiRequest(path, options = {}, retryAfterRefresh = true) {
  let response;
  try {
    response = await send(path, options);
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('The UniMarket API could not be reached. Check that the backend is running.');
  }

  if (response.status === 401 && retryAfterRefresh && accessToken && !path.startsWith('/api/v1/auth/')) {
    try {
      await refreshSession();
      return apiRequest(path, options, false);
    } catch (error) {
      setAccessToken(null);
      window.dispatchEvent(new CustomEvent('unimarket:auth-expired'));
      throw error;
    }
  }

  if (!response.ok) throw await toApiError(response);
  return parseResponse(response);
}

export async function loginRequest(credentials) {
  const response = await send('/api/v1/auth/login', {
    method: 'POST',
    body: credentials,
    auth: false,
  });
  if (!response.ok) throw await toApiError(response);
  const payload = await parseResponse(response);
  setAccessToken(payload.accessToken);
  return payload;
}

export async function registerRequest(details) {
  const response = await send('/api/v1/auth/register', {
    method: 'POST',
    body: details,
    auth: false,
  });
  if (!response.ok) throw await toApiError(response);
  return parseResponse(response);
}

export async function logoutRequest() {
  try {
    const response = await send('/api/v1/auth/logout', { method: 'POST', auth: false });
    if (!response.ok) throw await toApiError(response);
    return await parseResponse(response);
  } finally {
    setAccessToken(null);
  }
}
