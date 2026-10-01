import { useEffect, useState } from 'react';
import { apiRequest } from '../../js/api/client.js';
import { cancelEventRegistration, cancelHostedEvent, listHostedAttendees, listMyEventRegistrations, registerForEvent } from '../../js/api/events.js';
import { fetchCommunityEvents } from '../../js/api/bulletin.js';
import { useAuth } from '../auth/AuthContext.jsx';
import AppSidebar from '../components/AppSidebar.jsx';
import Icon from '../components/Icon.jsx';

const PAGE_SIZE = 9;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const DATE_FORMATTER = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'full', timeStyle: 'short' });
const PUBLISHED_FORMATTER = new Intl.DateTimeFormat('en-ZA', { dateStyle: 'medium' });
const EMPTY_EVENT = { title: '', content: '', location: '', eventStartsAt: '', eventCapacity: '' };

function formatDate(value, formatter = DATE_FORMATTER) { if (!value) return null; const date = new Date(value); return Number.isNaN(date.getTime()) ? null : formatter.format(date); }
function fallbackImage(event) { event.currentTarget.onerror = null; event.currentTarget.src = '/images/meetup.png'; }

function EventCard({ event, registration, attendees, busy, onRegistration, onAttendees, onCancelEvent, host }) {
  const startsAt = formatDate(event.eventStartsAt);
  const publishedAt = formatDate(event.publishedAt, PUBLISHED_FORMATTER);
  const capacity = event.eventCapacity == null ? `${event.confirmedAttendeeCount || 0} registered · unlimited capacity` : `${event.confirmedAttendeeCount || 0} of ${event.eventCapacity} confirmed`;
  const activeRegistration = ['CONFIRMED', 'WAITLISTED'].includes(registration?.status);
  const registrationLabel = registration?.status === 'CONFIRMED' ? 'Confirmed — cancel place' : registration?.status === 'WAITLISTED' ? `Waitlisted${registration.waitlistPosition ? ` (#${registration.waitlistPosition})` : ''} — cancel` : event.remainingCapacity === 0 ? 'Join waitlist' : 'Register for event';

  return <article className="community-event-card">
    <figure className="community-event-card__media"><img src={event.coverImageUrl || '/images/meetup.png'} alt={`${event.title} event banner`} onError={fallbackImage} /></figure>
    <div className="community-event-card__body">
      <div className="community-event-card__topline"><span>{event.eventCancelled ? 'Cancelled event' : 'Community event'}</span>{publishedAt && <time dateTime={event.publishedAt}>Posted {publishedAt}</time>}</div>
      <h2>{event.title}</h2><p className="community-event-card__content">{event.content}</p>
      <dl className="community-event-card__details"><div><dt>When</dt><dd>{startsAt ? <time dateTime={event.eventStartsAt}>{startsAt}</time> : 'Time to be announced'}</dd></div><div><dt><Icon name="pin" size={15} /> Where</dt><dd>{event.location || 'Location to be announced'}</dd></div><div><dt><Icon name="user" size={15} /> Hosted by</dt><dd>{event.authorDisplayName || 'A UniMarket community member'}</dd></div><div><dt>Attendance</dt><dd>{capacity}</dd></div></dl>
      {event.eventCancelled && <div className="form-alert form-alert--error" role="status">This event was cancelled. {event.eventCancellationReason}</div>}
      {!event.eventCancelled && (host ? <div className="event-card-actions"><button className="button button--dark" type="button" onClick={() => onAttendees(event.id)} disabled={busy}>Show attendees</button><button className="button button--outline" type="button" onClick={() => onCancelEvent(event.id)} disabled={busy}>Cancel event</button></div> : <div className="event-card-actions"><button className="button button--orange" type="button" onClick={() => onRegistration(event, activeRegistration)} disabled={busy}>{registrationLabel}</button></div>)}
      {registration && !host && <p className="event-registration-state" role="status">Your registration: <strong>{registration.status.replaceAll('_', ' ')}</strong></p>}
      {host && attendees && <div className="event-attendee-list"><strong>Attendees</strong>{attendees.length ? attendees.map((attendee) => <p key={attendee.registrationId}>{attendee.displayName} · {attendee.status.replaceAll('_', ' ')}{attendee.waitlistPosition ? ` (#${attendee.waitlistPosition})` : ''}</p>) : <p>No registrations yet.</p>}</div>}
    </div>
  </article>;
}

export default function CommunityPage() {
  const auth = useAuth();
  const [page, setPage] = useState(0); const [reloadKey, setReloadKey] = useState(0);
  const [state, setState] = useState({ status: 'loading', data: null, error: null });
  const [registrations, setRegistrations] = useState({}); const [attendeeLists, setAttendeeLists] = useState({}); const [busyId, setBusyId] = useState('');
  const [form, setForm] = useState(EMPTY_EVENT); const [eventImage, setEventImage] = useState(null); const [imagePreview, setImagePreview] = useState(''); const [imageInputKey, setImageInputKey] = useState(0);
  const [formStatus, setFormStatus] = useState({ error: '', success: '', submitting: false });

  useEffect(() => { const controller = new AbortController(); setState({ status: 'loading', data: null, error: null }); Promise.all([fetchCommunityEvents({ page, size: PAGE_SIZE, signal: controller.signal }), listMyEventRegistrations({ signal: controller.signal })]).then(([data, ownRegistrations]) => { setRegistrations(Object.fromEntries(ownRegistrations.map((registration) => [registration.eventId, registration]))); setState({ status: 'success', data, error: null }); }).catch((error) => { if (error.name !== 'AbortError') setState({ status: 'error', data: null, error }); }); return () => controller.abort(); }, [page, reloadKey]);
  useEffect(() => () => { if (imagePreview) URL.revokeObjectURL(imagePreview); }, [imagePreview]);

  const reload = () => setReloadKey((key) => key + 1);
  const changePage = (nextPage) => { setPage(nextPage); window.requestAnimationFrame(() => document.getElementById('community-events')?.scrollIntoView({ behavior: 'smooth' })); };
  const updateRegistration = async (event, isActive) => { setBusyId(event.id); try { const registration = isActive ? await cancelEventRegistration(event.id) : await registerForEvent(event.id); setRegistrations((current) => ({ ...current, [event.id]: registration })); reload(); } catch (error) { setState((current) => ({ ...current, error })); } finally { setBusyId(''); } };
  const showAttendees = async (eventId) => { setBusyId(eventId); try { const attendees = await listHostedAttendees(eventId); setAttendeeLists((current) => ({ ...current, [eventId]: attendees })); } catch (error) { setState((current) => ({ ...current, error })); } finally { setBusyId(''); } };
  const cancelHosted = async (eventId) => { const reason = window.prompt('Explain why this event is being cancelled.'); if (!reason?.trim()) return; setBusyId(eventId); try { await cancelHostedEvent(eventId, reason.trim()); reload(); } catch (error) { setState((current) => ({ ...current, error })); } finally { setBusyId(''); } };
  const chooseImage = (event) => { const file = event.target.files?.[0] || null; setFormStatus((current) => ({ ...current, error: '' })); if (file && (!file.type.startsWith('image/') || file.size > MAX_IMAGE_BYTES)) { event.target.value = ''; setEventImage(null); setFormStatus({ error: 'Choose a PNG, JPEG, GIF or WebP image no larger than 5 MB.', success: '', submitting: false }); return; } if (imagePreview) URL.revokeObjectURL(imagePreview); setEventImage(file); setImagePreview(file ? URL.createObjectURL(file) : ''); };
  const createEvent = async (event) => { event.preventDefault(); setFormStatus({ error: '', success: '', submitting: true }); try { let coverImageUrl = null; if (eventImage) { const upload = new FormData(); upload.append('image', eventImage); const stored = await apiRequest('/api/v1/bulletin/images/upload', { method: 'POST', body: upload }); coverImageUrl = stored.imageUrl; } await apiRequest('/api/v1/bulletin', { method: 'POST', body: { type: 'EVENT', title: form.title, content: form.content, location: form.location || null, coverImageUrl, eventStartsAt: new Date(form.eventStartsAt).toISOString(), eventCapacity: form.eventCapacity ? Number(form.eventCapacity) : null, expiresAt: null, publishNow: true } }); if (imagePreview) URL.revokeObjectURL(imagePreview); setForm(EMPTY_EVENT); setEventImage(null); setImagePreview(''); setImageInputKey((key) => key + 1); setFormStatus({ error: '', success: 'Your event and banner are published.', submitting: false }); reload(); } catch (error) { setFormStatus({ error: error.message, success: '', submitting: false }); } };
  const updateForm = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  const data = state.data; const hasEvents = state.status === 'success' && data.content.length > 0;

  return <main className="community-page" id="main-content"><div className="container portal-shell portal-shell--page"><AppSidebar /><div className="portal-main">
    <section className="community-page__hero" aria-label="Community"><div className="container community-page__hero-grid"><div><p className="eyebrow eyebrow--line">People. Ideas. Opportunities.</p><p>Discover meetups, workshops and events created by people around you.</p></div><figure><img src="/images/meetup.png" alt="Students meeting in a sunny campus courtyard" /></figure></div></section>
    <section className="community-events-section community-events-section--host"><div className="container"><div className="dashboard-panel community-host-form"><div className="dashboard-panel__heading"><div><p className="eyebrow eyebrow--line">Host an event</p><h2>Publish a community event</h2></div></div>{formStatus.error && <div className="form-alert form-alert--error" role="alert">{formStatus.error}</div>}{formStatus.success && <div className="form-alert form-alert--success" role="status">{formStatus.success}</div>}<form className="platform-form platform-form--grid" onSubmit={createEvent}>
      <label>Event title<input required maxLength="160" name="title" value={form.title} onChange={updateForm} /></label><label>Starts at<input required name="eventStartsAt" type="datetime-local" value={form.eventStartsAt} onChange={updateForm} /></label><label className="form-span-2">Description<textarea required maxLength="5000" name="content" rows="3" value={form.content} onChange={updateForm} /></label><label>Location<input maxLength="200" name="location" value={form.location} onChange={updateForm} /></label><label>Maximum attendees <span>(optional)</span><input min="1" name="eventCapacity" type="number" value={form.eventCapacity} onChange={updateForm} /></label>
      <label className="form-span-2 image-upload-field">Event banner <span>(PNG, JPEG, GIF or WebP · max 5 MB)</span><input key={imageInputKey} accept="image/png,image/jpeg,image/gif,image/webp" type="file" onChange={chooseImage} />{imagePreview && <span className="image-upload-preview"><img src={imagePreview} alt="Selected event banner preview" /><small>{eventImage?.name}</small></span>}</label>
      <button className="button button--orange form-span-2" disabled={formStatus.submitting} type="submit">{formStatus.submitting ? 'Publishing…' : 'Publish event'}</button>
    </form></div></div></section>
    <section className="community-events-section" id="community-events" aria-labelledby="community-events-title" aria-busy={state.status === 'loading'}><div className="container"><div className="community-events-heading"><div><p className="eyebrow eyebrow--line">What is happening</p><h2 id="community-events-title">Community events & meetups</h2></div>{state.status === 'success' && <p>{data.totalElements} current {data.totalElements === 1 ? 'event' : 'events'}</p>}</div>
      {state.status === 'loading' && <div className="community-page-state" role="status"><span className="route-loading__mark" /><h2>Loading community events…</h2></div>}{state.status === 'error' && <div className="community-page-state" role="alert"><h2>Community events could not be loaded.</h2><p>{state.error?.message || 'Please try again.'}</p><button className="button button--dark" type="button" onClick={reload}>Try again</button></div>}{state.status === 'success' && state.error && <div className="form-alert form-alert--error" role="alert">{state.error.message}</div>}{state.status === 'success' && !hasEvents && <div className="community-page-state"><h2>No current events yet.</h2><p>Host the first event for your community above.</p></div>}
      {hasEvents && <><div className="community-events-grid">{data.content.map((event) => <EventCard key={event.id} event={event} registration={registrations[event.id]} attendees={attendeeLists[event.id]} busy={busyId === event.id} host={event.authorId === auth.account?.id} onRegistration={updateRegistration} onAttendees={showAttendees} onCancelEvent={cancelHosted} />)}</div>{data.totalPages > 1 && <nav className="catalogue-pagination" aria-label="Community event pages"><button type="button" disabled={page === 0} onClick={() => changePage(page - 1)}>Previous</button><span>Page <strong>{page + 1}</strong> of <strong>{data.totalPages}</strong></span><button type="button" disabled={page + 1 >= data.totalPages} onClick={() => changePage(page + 1)}>Next</button></nav>}</>}
    </div></section>
  </div></div></main>;
}
