package com.example.unimarket.service.impl;

import com.example.unimarket.domain.BulletinPost;
import com.example.unimarket.domain.EventRegistration;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.BulletinPostStatus;
import com.example.unimarket.domain.enums.BulletinPostType;
import com.example.unimarket.domain.enums.EventRegistrationStatus;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.BulletinPostFactory;
import com.example.unimarket.factory.EventRegistrationFactory;
import com.example.unimarket.repository.IBulletinPostRepository;
import com.example.unimarket.repository.IEventRegistrationRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.response.EventAttendeeResponse;
import com.example.unimarket.response.EventRegistrationResponse;
import com.example.unimarket.service.IEventRegistrationService;
import com.example.unimarket.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventRegistrationServiceImpl implements IEventRegistrationService {
    private final IBulletinPostRepository bulletinRepository;
    private final IEventRegistrationRepository registrationRepository;
    private final IUserAccountRepository accountRepository;
    private final IUserProfileRepository profileRepository;
    private final INotificationService notificationService;

    public EventRegistrationServiceImpl(IBulletinPostRepository bulletinRepository,
                                        IEventRegistrationRepository registrationRepository,
                                        IUserAccountRepository accountRepository,
                                        IUserProfileRepository profileRepository,
                                        INotificationService notificationService) {
        this.bulletinRepository = bulletinRepository;
        this.registrationRepository = registrationRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public EventRegistrationResponse register(UUID attendeeId, UUID eventId) {
        ensureActive(attendeeId);
        BulletinPost event = requiredOpenEventForUpdate(eventId);
        if (event.getAuthorId().equals(attendeeId)) {
            throw new ValidationException("Event hosts are already counted separately and cannot register as attendees.");
        }
        EventRegistration existing = registrationRepository.readByEventIdAndAttendeeId(eventId, attendeeId);
        if (existing != null && existing.isActive()) return response(existing, event);

        Instant now = Instant.now();
        EventRegistrationStatus next = hasCapacity(event)
                ? EventRegistrationStatus.CONFIRMED : EventRegistrationStatus.WAITLISTED;
        EventRegistration saved;
        if (existing == null) {
            saved = registrationRepository.create(EventRegistrationFactory.create(eventId, attendeeId, next, now));
        } else {
            if (!existing.reregister(next, now)) throw new ValidationException("This registration cannot be restored.");
            saved = registrationRepository.update(existing);
        }
        notifyHostOfRegistration(event, saved);
        return response(saved, event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventRegistrationResponse getOwnRegistration(UUID attendeeId, UUID eventId) {
        EventRegistration registration = registrationRepository.readByEventIdAndAttendeeId(eventId, attendeeId);
        if (registration == null) throw ResourceNotFoundException.of("Event registration");
        BulletinPost event = bulletinRepository.read(eventId);
        if (event == null) throw ResourceNotFoundException.of("Event");
        return response(registration, event);
    }

    @Override
    @Transactional
    public EventRegistrationResponse cancelOwnRegistration(UUID attendeeId, UUID eventId) {
        BulletinPost event = bulletinRepository.readByIdForUpdate(eventId);
        if (event == null || event.getType() != BulletinPostType.EVENT) throw ResourceNotFoundException.of("Event");
        EventRegistration registration = registrationRepository.readByEventIdAndAttendeeId(eventId, attendeeId);
        if (registration == null || !registration.isActive()) throw new ValidationException("You do not have an active registration for this event.");
        boolean releasedConfirmedSeat = registration.getStatus() == EventRegistrationStatus.CONFIRMED;
        if (!registration.cancel(Instant.now(), false)) throw new ValidationException("This registration cannot be cancelled.");
        EventRegistration saved = registrationRepository.update(registration);
        if (releasedConfirmedSeat) promoteFirstWaitlisted(event);
        notificationService.send(event.getAuthorId(), NotificationType.EVENT_ATTENDEE_CANCELLED,
                "Event attendee cancelled", "An attendee cancelled their registration for " + event.getTitle() + ".",
                "BULLETIN_EVENT", event.getId(), "event:" + event.getId() + ":cancel:" + saved.getId());
        return response(saved, event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> listOwnRegistrations(UUID attendeeId) {
        return registrationRepository.readByAttendeeId(attendeeId).stream().map(registration -> {
            BulletinPost event = bulletinRepository.read(registration.getBulletinPostId());
            return event == null ? null : response(registration, event);
        }).filter(value -> value != null).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventAttendeeResponse> listHostedAttendees(UUID hostId, UUID eventId) {
        BulletinPost event = ownedEvent(hostId, eventId);
        return registrationRepository.readByEventId(event.getId()).stream().map(registration -> {
            Integer position = registration.getStatus() == EventRegistrationStatus.WAITLISTED
                    ? waitlistPosition(event.getId(), registration.getId()) : null;
            UserProfile profile = profileRepository.readByUserId(registration.getAttendeeId());
            return EventAttendeeResponse.from(registration, profile, position);
        }).toList();
    }

    @Override
    @Transactional
    public void cancelHostedEvent(UUID hostId, UUID eventId, String reason) {
        BulletinPost event = bulletinRepository.readByIdForUpdate(eventId);
        if (event == null || !event.getAuthorId().equals(hostId) || event.getType() != BulletinPostType.EVENT) {
            throw ResourceNotFoundException.of("Event");
        }
        if (BulletinPostFactory.cancelEvent(event, reason) == null) {
            throw new ValidationException("Only a live event can be cancelled once.");
        }
        bulletinRepository.update(event);
        registrationRepository.readByEventIdAndStatuses(eventId,
                List.of(EventRegistrationStatus.CONFIRMED, EventRegistrationStatus.WAITLISTED)).forEach(registration -> {
            registration.cancel(Instant.now(), true);
            registrationRepository.update(registration);
            notificationService.send(registration.getAttendeeId(), NotificationType.EVENT_CANCELLED,
                    "Event cancelled", event.getTitle() + " was cancelled by its host: " + event.getEventCancellationReason(),
                    "BULLETIN_EVENT", event.getId(), "event:" + event.getId() + ":cancelled:" + registration.getId());
        });
    }

    private BulletinPost requiredOpenEventForUpdate(UUID eventId) {
        BulletinPost event = bulletinRepository.readByIdForUpdate(eventId);
        Instant now = Instant.now();
        if (event == null || event.getType() != BulletinPostType.EVENT || event.getStatus() != BulletinPostStatus.PUBLISHED
                || event.isEventCancelled() || event.getEventStartsAt() == null || !event.getEventStartsAt().isAfter(now)
                || event.getExpiresAt() != null && !event.getExpiresAt().isAfter(now)) {
            throw new ValidationException("This event is not open for registration.");
        }
        return event;
    }

    private BulletinPost ownedEvent(UUID hostId, UUID eventId) {
        BulletinPost event = bulletinRepository.read(eventId);
        if (event == null || !hostId.equals(event.getAuthorId()) || event.getType() != BulletinPostType.EVENT) {
            throw ResourceNotFoundException.of("Event");
        }
        return event;
    }

    private void ensureActive(UUID userId) {
        UserAccount account = accountRepository.read(userId);
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can register for events.");
        }
    }

    private boolean hasCapacity(BulletinPost event) {
        return event.getEventCapacity() == null || registrationRepository.countByEventIdAndStatus(event.getId(),
                EventRegistrationStatus.CONFIRMED) < event.getEventCapacity();
    }

    private void promoteFirstWaitlisted(BulletinPost event) {
        if (!hasCapacity(event)) return;
        EventRegistration candidate = registrationRepository.readFirstWaitlisted(event.getId());
        if (candidate == null || !candidate.confirm(Instant.now())) return;
        registrationRepository.update(candidate);
        notificationService.send(candidate.getAttendeeId(), NotificationType.EVENT_WAITLIST_PROMOTED,
                "You have a confirmed event place", "A place became available for " + event.getTitle() + ".",
                "BULLETIN_EVENT", event.getId(), "event:" + event.getId() + ":promoted:" + candidate.getId());
    }

    private EventRegistrationResponse response(EventRegistration registration, BulletinPost event) {
        long confirmed = registrationRepository.countByEventIdAndStatus(event.getId(), EventRegistrationStatus.CONFIRMED);
        Integer position = registration.getStatus() == EventRegistrationStatus.WAITLISTED
                ? waitlistPosition(event.getId(), registration.getId()) : null;
        return EventRegistrationResponse.from(registration, event.getEventCapacity(), confirmed, position);
    }

    private Integer waitlistPosition(UUID eventId, UUID registrationId) {
        List<EventRegistration> waitlist = registrationRepository.readByEventIdAndStatuses(eventId,
                List.of(EventRegistrationStatus.WAITLISTED));
        for (int index = 0; index < waitlist.size(); index++) {
            if (waitlist.get(index).getId().equals(registrationId)) return index + 1;
        }
        return null;
    }

    private void notifyHostOfRegistration(BulletinPost event, EventRegistration registration) {
        notificationService.send(event.getAuthorId(), NotificationType.EVENT_ATTENDEE_REGISTERED,
                "New event registration", "A member " + registration.getStatus().name().toLowerCase()
                        + " for " + event.getTitle() + ".", "BULLETIN_EVENT", event.getId(),
                "event:" + event.getId() + ":registration:" + registration.getId() + ":" + registration.getStatus());
    }
}
