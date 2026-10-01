import { ApiError, apiRequest } from './client.js';

export function registerForEvent(eventId) {
  return apiRequest(`/api/v1/events/${eventId}/registrations`, { method: 'POST' });
}

export function getMyEventRegistration(eventId, options = {}) {
  return apiRequest(`/api/v1/events/${eventId}/registrations/me`, options);
}

export function cancelEventRegistration(eventId) {
  return apiRequest(`/api/v1/events/${eventId}/registrations/me`, { method: 'DELETE' });
}

export function listMyEventRegistrations(options = {}) {
  return apiRequest('/api/v1/events/registrations/mine', options).then((payload) => {
    if (!Array.isArray(payload)) throw new ApiError('Your event registrations returned an unexpected response.');
    return payload;
  });
}

export function listHostedAttendees(eventId, options = {}) {
  return apiRequest(`/api/v1/events/${eventId}/attendees`, options).then((payload) => {
    if (!Array.isArray(payload)) throw new ApiError('The attendee list returned an unexpected response.');
    return payload;
  });
}

export function cancelHostedEvent(eventId, reason) {
  return apiRequest(`/api/v1/events/${eventId}/cancel`, { method: 'PATCH', body: { reason } });
}
