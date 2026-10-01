# Requirements Document

## Introduction

UniMarket is a trusted campus-community marketplace for South African higher education. Students, faculty, residents, individual sellers, and registered businesses buy and sell goods and services, supported by community announcements, ratings, moderation, and notifications.

This spec covers the **backend only**, to be built before the React frontend. The stack is Java 21, Spring Boot, Spring Security, Spring Data JPA, and MySQL, following the project's existing domain-driven layering (`domain`, `factory`, `repository`, `request`, `response`, `service`, `service.impl`, `config`, `controller`, `util`). Authentication uses local accounts with verified email, short-lived JWT access tokens, and rotated refresh tokens. Payments are **simulated**: no real payment gateway is integrated and no real card or banking data is ever collected or stored.

The detailed entity catalogue, attribute tables, ER diagram, and JWT flow diagrams live in `README.md`. This document defines the behavioural requirements those structures must satisfy.

### Current repository state

The repository is a generated skeleton. `AuthController`, `AuthServiceImpl`, `IAuthService`, `AuthResponse`, `SecurityConfig`, `JwtConfig`, `PasswordEncoderConfig`, and `CorsConfig` are empty classes, and the `domain`, `repository`, `request`, and `factory` packages are empty. No entity, endpoint, or security flow exists yet. Two existing issues must be corrected during implementation: `application.properties` contains a plaintext MySQL root password with `ddl-auto=update`, and the `until` package is a misspelling of `util`.

### Separation of concerns

Three concepts are kept deliberately separate throughout these requirements, because collapsing them into a single `userType` field would prevent a student from also selling and would let a self-declared label act as a trust signal:

- **Authentication** — who controls the account and its verified email address
- **Authorization** — what the account may do (`BUYER`, `SELLER`, `MODERATOR`, `ADMIN`)
- **Trust status** — what affiliation or business evidence has actually been verified (student, faculty, community member, individual seller, verified business)

### Research findings carried into these requirements

Universities South Africa represents the 26 public universities, but institutions publish student email conventions inconsistently and change them over time. Current student mailbox formats were confirmed from official or institution-hosted sources for a subset of institutions; the remainder must be treated as unconfirmed rather than inferred by pattern-matching. Separately, there is no authoritative South African vendor email extension, so a professional-looking domain proves nothing about business registration. Both findings are encoded as explicit requirements below.

### Scope boundaries

**In scope:** identity and access, email and institutional verification, seller and business verification, catalog and inventory, cart and multi-seller checkout, simulated payment, fulfilment, verified-purchase reviews, community bulletin, notifications, reporting and moderation, rule-based fraud assessment, audit trail, and the security and configuration foundation.

**Deferred unless time remains:** wishlists, promotions, referrals, loyalty points, direct messaging, comments and polls, Redis caching, extraction into microservices, automated CIPC API integration, and machine-learning fraud detection.

**Architecture decision:** the MVP is a modular monolith with clear bounded contexts, so boundaries can later be extracted into services without redesigning the data model.

## Requirements

### Requirement 1: Account registration and email verification

**User Story:** As a new visitor, I want to create an account and confirm my email address, so that I can participate in the marketplace and other members can trust that my contact address is real.

#### Acceptance Criteria

1. WHEN a visitor submits a registration request THEN the system SHALL normalise the email address by trimming whitespace and converting it to lowercase before any validation or storage.
2. WHEN a registration request is received THEN the system SHALL validate it server-side and SHALL reject it with a structured validation error if any required field is missing, malformed, or exceeds its defined length limit.
3. WHEN a password is submitted THEN the system SHALL require between 12 and 128 characters, SHALL accept passphrases including spaces and Unicode characters, and SHALL NOT impose arbitrary character-composition or forced-rotation rules.
4. WHEN a valid registration is accepted THEN the system SHALL store the password only as a BCrypt hash and SHALL NEVER store, log, or return the plaintext password.
5. WHEN a valid registration is accepted THEN the system SHALL create the account with status `PENDING_EMAIL`, SHALL grant the `BUYER` role, SHALL record the accepted terms and privacy versions with their timestamps, and SHALL respond with HTTP 202 Accepted.
6. WHEN an account is created THEN the system SHALL generate a single-use email verification token, SHALL persist only a hash of that token with a short expiry, and SHALL send the raw token to the registered address exactly once.
7. IF a registration request uses an email address that already exists THEN the system SHALL NOT reveal in the public response whether that address is registered.
8. WHEN a valid, unexpired, unconsumed verification token is submitted THEN the system SHALL atomically consume the token, SHALL set the email-verified timestamp, SHALL transition the account to `ACTIVE`, and SHALL invalidate any other outstanding verification tokens for that account.
9. IF a verification token is expired, already consumed, or unrecognised THEN the system SHALL reject the request without indicating which condition applied.
10. WHILE an account remains in `PENDING_EMAIL` status THEN the system SHALL deny access to all authenticated marketplace endpoints.
11. WHEN a verification email resend is requested THEN the system SHALL rate-limit the request per account and per source.

### Requirement 2: Login and JWT access tokens

**User Story:** As a registered member, I want to log in and receive a short-lived access token, so that I can use the application securely without repeatedly sending my password.

#### Acceptance Criteria

1. WHEN a login request is received THEN the system SHALL apply rate limiting per account, per email, and per source before evaluating credentials.
2. WHEN login credentials are evaluated THEN the system SHALL verify the submitted password against the stored BCrypt hash using the password encoder's constant-time comparison.
3. IF credentials are invalid, the account does not exist, or the account is not permitted to log in THEN the system SHALL return a single generic authentication failure response that does not distinguish between those causes.
4. WHEN a login succeeds THEN the system SHALL issue a JWT access token signed with an asymmetric algorithm (RS256 or ES256) with a lifetime of no more than 15 minutes.
5. WHEN an access token is issued THEN the system SHALL include the `iss`, `aud`, `sub`, `sid`, `jti`, `iat`, `nbf`, and `exp` claims together with the account's active roles and email-verified state.
6. WHEN an access token is issued THEN the system SHALL NOT include addresses, password hashes, student numbers, business registration data, ratings, or any other mutable profile detail in its claims.
7. WHEN a protected endpoint receives a bearer token THEN the system SHALL validate the signature, issuer, audience, signing algorithm, and expiry before populating the security context, and SHALL reject the request if any check fails.
8. WHEN a login attempt completes THEN the system SHALL record the outcome for throttling and fraud analysis without persisting the submitted password.
9. WHEN consecutive failed login attempts exceed the configured threshold THEN the system SHALL apply a temporary lockout, and WHILE that lockout is active the system SHALL deny authentication for that account.
10. WHEN a login response is returned THEN the system SHALL include the access token, its type, its expiry in seconds, and a safe account summary, and SHALL NOT include the refresh token in the response body.

### Requirement 3: Refresh token rotation and session management

**User Story:** As a member, I want long-running sessions that renew safely and can be revoked, so that a stolen token cannot be reused indefinitely and I can end sessions I no longer recognise.

#### Acceptance Criteria

1. WHEN a login succeeds THEN the system SHALL create a server-side session record storing only a hash of the refresh token, its token family identifier, issue time, and expiry.
2. WHEN a refresh token is issued to a browser client THEN the system SHALL deliver it in a cookie marked `Secure` and `HttpOnly`, with a deliberate `SameSite` attribute and a bounded path.
3. WHEN a refresh request presents a valid, unexpired, unrevoked refresh token THEN the system SHALL rotate it within a single transaction by revoking the presented session, creating a replacement session in the same token family, and issuing a new access token.
4. IF a refresh token that has already been rotated is presented again THEN the system SHALL treat this as token reuse, SHALL revoke every session in that token family, and SHALL require re-authentication.
5. IF a refresh token is expired, revoked, or unrecognised THEN the system SHALL reject the refresh request without issuing any token.
6. WHEN a logout request is received THEN the system SHALL revoke the current session, record the revocation reason, and expire the refresh cookie.
7. WHEN a logout-all request is received THEN the system SHALL revoke every active session belonging to that account.
8. WHEN a password is changed or reset THEN the system SHALL update the credentials-changed timestamp and SHALL revoke all active sessions for that account.
9. IF an account becomes `LOCKED`, `SUSPENDED`, or `CLOSED` THEN the system SHALL deny refresh for that account even while a previously issued access token has not yet expired.
10. WHEN a member requests their session list THEN the system SHALL return non-sensitive session metadata only and SHALL NEVER return stored token hashes.

### Requirement 4: Password reset and credential changes

**User Story:** As a member who has forgotten my password or needs to change my email, I want a secure recovery path, so that I can regain access without creating an opening for an attacker.

#### Acceptance Criteria

1. WHEN a password reset is requested THEN the system SHALL respond identically whether or not the address is registered.
2. WHEN a password reset is requested for an existing account THEN the system SHALL generate a single-use reset token, SHALL store only its hash with a short expiry, and SHALL send the raw token to the verified address.
3. WHEN a valid reset token and new password are submitted THEN the system SHALL atomically consume the token, update the stored hash, revoke all sessions, and invalidate all other outstanding reset tokens for that account.
4. WHEN an authenticated member requests a password change THEN the system SHALL require the current password before applying the change.
5. WHEN an email change is requested THEN the system SHALL send a verification token to the proposed new address and SHALL NOT replace the login email until that address is verified.
6. WHEN a login email is successfully changed THEN the system SHALL re-evaluate any affiliation badges that depended on the previous address.
7. WHEN a reset or verification token is submitted repeatedly THEN the system SHALL count attempts and rate-limit further submissions.

### Requirement 5: Role-based authorization and ownership checks

**User Story:** As the platform owner, I want permissions separated from self-declared identity, so that members can hold several capabilities at once and privileged access is never self-assigned.

#### Acceptance Criteria

1. WHEN an account becomes active THEN the system SHALL grant the `BUYER` role automatically.
2. WHERE roles are assigned THEN the system SHALL support an account holding several roles simultaneously, including `BUYER` together with `SELLER`.
3. WHEN a privileged role such as `MODERATOR` or `ADMIN` is granted THEN the system SHALL record the granting actor and timestamp, and SHALL NOT allow the role to be self-selected during registration.
4. WHEN a role is revoked THEN the system SHALL record the revocation time and SHALL retain the historical assignment rather than deleting it.
5. WHERE role assignments are stored THEN the system SHALL permit at most one active assignment per account and role combination.
6. WHEN a request targets a resource owned by a specific account THEN the system SHALL verify ownership in the service or authorization layer and SHALL NOT treat role membership alone as sufficient.
7. IF an authenticated member attempts to read or modify another member's cart, order, address, notification, or seller resource THEN the system SHALL deny the request.
8. WHEN authentication is missing or invalid THEN the system SHALL return HTTP 401, and WHEN authentication is valid but permission is insufficient THEN the system SHALL return HTTP 403.
9. WHERE public endpoints are defined THEN the system SHALL expose only registration, login, email verification, password reset, listing browsing, and published bulletin reads without authentication.

### Requirement 6: Institution registry and email domain policy

**User Story:** As an administrator, I want supported institutions and their email domains managed as data with recorded evidence, so that the platform can adapt when a university changes its conventions without a code change.

#### Acceptance Criteria

1. WHERE the system recognises institutions THEN it SHALL store them as data records with an official name, short name, institution type of `UNIVERSITY` or `UNIVERSITY_OF_TECHNOLOGY`, public website domain, and an active flag.
2. WHERE the system recognises institutional email domains THEN it SHALL store each domain as a data record and SHALL NOT hard-code any university email domain in application code.
3. WHEN an email domain record is created THEN the system SHALL require an affiliation type, an enforcement mode, a source URL evidencing the format, and the date that source was checked.
4. WHERE an email domain defines an enforcement mode THEN the system SHALL support `DOMAIN_ONLY`, `ADVISORY_PATTERN`, `STRICT_PATTERN`, and `MANUAL_REVIEW`.
5. WHEN an institutional email is matched against the registry THEN the system SHALL match the longest active domain suffix on an exact label boundary, so that a lookalike domain such as `fake-myuct.ac.za` does not match `myuct.ac.za`.
6. IF an institutional email matches an active domain but fails that domain's local-part pattern under `ADVISORY_PATTERN` THEN the system SHALL route the claim to manual review rather than rejecting a potentially legitimate mailbox.
7. WHERE initial seed data is created THEN the system SHALL activate only those institutions whose current student mailbox format is evidenced by an official or institution-hosted source, namely University of Cape Town, University of the Free State, University of KwaZulu-Natal, North-West University, University of Pretoria, Rhodes University, Stellenbosch University, University of the Witwatersrand, University of the Western Cape, Nelson Mandela University, University of South Africa, Cape Peninsula University of Technology, and Durban University of Technology.
8. WHERE an institution's current student mailbox format cannot be confirmed from an official source THEN the system SHALL seed that institution with its domain policy inactive and marked unconfirmed, and SHALL NOT infer a format by pattern-matching other universities.
9. IF an email domain record is inactive or unconfirmed THEN the system SHALL NOT grant any automatic institutional badge from that domain.
10. WHEN an administrator activates an email domain THEN the system SHALL require the source URL and check date to be recorded before activation takes effect.

### Requirement 7: Institutional affiliation verification

**User Story:** As a student or staff member, I want to verify my university email, so that I receive a campus affiliation badge that other members can trust.

#### Acceptance Criteria

1. WHEN a member claims an institutional affiliation THEN the system SHALL create the affiliation with status `PENDING` and SHALL send a single-use verification token to the claimed institutional address.
2. WHEN an institutional verification token is successfully consumed THEN the system SHALL set the affiliation status to `VERIFIED`, record the verification time and method, and apply the corresponding trust badge.
3. WHEN an affiliation is verified THEN the system SHALL grant a trust badge only and SHALL NOT grant any authorization role as a result of institutional verification.
4. WHERE affiliation types are recorded THEN the system SHALL support `STUDENT`, `FACULTY`, `ALUMNI`, and `UNKNOWN`.
5. IF a domain's affiliation type is shared or ambiguous between students and staff THEN the system SHALL route the claim to manual review before assigning a faculty badge.
6. WHERE affiliations are stored THEN the system SHALL enforce that one institutional email address is actively verified for at most one account.
7. WHERE affiliations are stored THEN the system SHALL prevent duplicate active affiliations for the same account and institution.
8. WHEN an affiliation is verified THEN the system SHALL treat it as evidence of mailbox control only and SHALL NOT represent it as proof of legal identity, current enrolment, age, or academic standing.
9. WHERE an affiliation carries an expiry THEN the system SHALL transition it to `EXPIRED` after that date and SHALL require reverification before restoring the badge.
10. WHEN a member submits a legitimate alias or an address that fails an outdated pattern THEN the system SHALL provide a manual review route rather than a permanent rejection.

### Requirement 8: Seller onboarding

**User Story:** As a member who wants to sell, I want to create a seller profile, so that I can publish listings without having to claim I am a registered company.

#### Acceptance Criteria

1. WHEN a member applies to sell THEN the system SHALL require a seller profile before any listing can be published.
2. WHEN a seller profile is created THEN the system SHALL require a seller type of either `INDIVIDUAL` or `REGISTERED_BUSINESS`.
3. WHERE a seller profile has type `INDIVIDUAL` THEN the system SHALL allow the profile to become active without any business registration evidence.
4. WHEN a seller profile is accepted THEN the system SHALL grant the `SELLER` role to that account.
5. WHERE seller profiles are stored THEN the system SHALL permit at most one seller profile per account.
6. WHILE a seller profile is in status `DRAFT`, `PENDING_REVIEW`, `SUSPENDED`, `REJECTED`, or `CLOSED` THEN the system SHALL prevent that seller's listings from being publicly visible.
7. WHERE a seller profile stores aggregate reputation values THEN the system SHALL treat them as cached projections derived from published reviews and completed orders and SHALL NOT treat them as authoritative.
8. IF an account's email is not verified THEN the system SHALL NOT allow seller onboarding to complete.

### Requirement 9: Registered business verification

**User Story:** As a registered business, I want to submit my CIPC registration evidence for review, so that buyers can see a verified business badge that reflects a real check.

#### Acceptance Criteria

1. WHERE a seller profile has type `REGISTERED_BUSINESS` THEN the system SHALL require a business profile containing legal name, CIPC registration number, business contact email, and business phone number.
2. WHEN a business registration number is submitted THEN the system SHALL validate only its structural shape and SHALL NOT treat a syntactically valid number as proof of registration.
3. WHEN a business verification is submitted THEN the system SHALL create a verification case with status `OPEN` and SHALL require moderator review before any badge is granted.
4. WHEN a supporting document is uploaded THEN the system SHALL validate its content type against a server-side allowlist, enforce a maximum size, record a SHA-256 checksum, and record a malware scan status.
5. WHERE verification documents are stored THEN the system SHALL persist only metadata and a private storage key in the database, SHALL NOT store document bytes in MySQL, and SHALL restrict retrieval to authorised moderators and administrators.
6. WHEN a moderator approves a business verification case THEN the system SHALL activate the seller profile, set the verified business badge, and record the reviewing actor and decision time.
7. IF a moderator rejects a verification case THEN the system SHALL require a decision reason and SHALL NOT set the verified business badge.
8. WHERE a business registration number or VAT number has been approved THEN the system SHALL enforce that it is not reused by another active business profile.
9. WHERE VAT information is collected THEN the system SHALL treat VAT registration as optional, and WHEN a VAT number is supplied THEN the system SHALL validate that it is a ten-digit value.
10. WHEN evaluating a business claim THEN the system SHALL NOT infer legitimacy from the email domain and SHALL accept any verified mailbox regardless of whether it uses a company domain or a free email provider.
11. WHERE business verification is implemented for the MVP THEN the system SHALL support manual moderator comparison against official CIPC records and SHALL keep the workflow unchanged if an automated lookup is added later.

### Requirement 10: Catalog, listings, and inventory

**User Story:** As a seller, I want to publish and manage listings with accurate stock, so that buyers see what is genuinely available.

#### Acceptance Criteria

1. WHERE listings are classified THEN the system SHALL support a hierarchical category structure with unique slugs and an active flag.
2. WHEN a seller creates a listing THEN the system SHALL require a category, a listing type of `PHYSICAL_GOOD`, `DIGITAL_GOOD`, or `SERVICE`, a title, a description, a non-negative price, and at least one supported fulfilment option.
3. WHERE prices are stored THEN the system SHALL use a fixed-precision decimal type with two decimal places and an ISO currency code, SHALL default to `ZAR` for the MVP, and SHALL NOT use floating-point types for money.
4. WHEN listing content is submitted THEN the system SHALL sanitise it before storage or display and SHALL enforce defined maximum lengths.
5. WHERE a listing records location THEN the system SHALL store only an approximate campus or area description and SHALL NOT publish a seller's private residential address on a listing.
6. WHEN a listing image is uploaded THEN the system SHALL require alternative text for accessibility, record a moderation status, and allow at most one primary image per listing.
7. WHERE a listing represents quantity-based stock THEN the system SHALL maintain an inventory record holding available quantity, reserved quantity, and a low-stock threshold.
8. WHERE inventory is maintained THEN the system SHALL enforce that reserved quantity never exceeds available quantity and that neither value becomes negative.
9. WHEN concurrent requests attempt to reserve the last available unit THEN the system SHALL prevent overselling using optimistic locking or an atomic conditional update and SHALL NOT rely on a read-then-write check alone.
10. WHEN a listing is withdrawn THEN the system SHALL apply a soft deletion that preserves historical order references.
11. WHERE listing queries are exposed THEN the system SHALL require pagination and SHALL support filtering by category, price, status, and seller.

### Requirement 11: Cart and multi-seller checkout

**User Story:** As a buyer, I want to add items from different sellers to one cart and check out once, so that a single purchase can span several sellers while each seller manages their own fulfilment.

#### Acceptance Criteria

1. WHERE carts are stored THEN the system SHALL maintain at most one active cart per buyer.
2. WHEN an item is added to a cart THEN the system SHALL require a quantity greater than zero and a fulfilment method supported by that listing.
3. WHERE cart items are stored THEN the system SHALL treat the combination of cart, listing, and selected fulfilment method as unique.
4. WHEN checkout begins THEN the system SHALL re-read authoritative listing price, listing status, seller status, and available stock, and SHALL NOT charge based on the price captured when the item was added.
5. IF a listing's price changed, its status changed, its seller became inactive, or stock became insufficient THEN the system SHALL block checkout for that item and report the specific reason to the buyer.
6. WHEN a checkout is created THEN the system SHALL calculate all monetary totals server-side and SHALL ignore any client-supplied totals.
7. WHEN a checkout contains items from several sellers THEN the system SHALL create one parent checkout order for the buyer and one seller order per participating seller.
8. WHEN order items are created THEN the system SHALL store immutable snapshots of listing title, listing type, unit price, quantity, line total, condition, primary image key, and seller name.
9. WHEN an order requires delivery THEN the system SHALL store an immutable address snapshot on the order, and subsequent edits to the member's saved address SHALL NOT alter any existing order.
10. IF a buyer attempts to purchase a listing belonging to their own seller profile THEN the system SHALL reject the checkout.
11. WHERE order identifiers are exposed THEN the system SHALL generate unique human-readable order numbers separate from internal primary keys.

### Requirement 12: Simulated payment

**User Story:** As a project team, we want a realistic but entirely simulated payment step, so that the full purchase journey can be demonstrated without integrating a payment gateway or handling real financial data.

#### Acceptance Criteria

1. WHERE payment is implemented THEN the system SHALL simulate the outcome internally and SHALL NOT call any external payment gateway.
2. WHEN a payment is initiated THEN the system SHALL accept only an allowlisted scenario token from the client and SHALL NOT accept or process a card number, expiry, CVV, bank account number, banking password, or banking one-time PIN.
3. WHERE payment records are stored THEN the system SHALL NOT persist any real or apparent cardholder or banking credential.
4. WHERE payment scenarios are supported THEN the system SHALL provide at least `SUCCESS`, `DECLINED`, `TIMEOUT`, `INSUFFICIENT_FUNDS`, and `REVIEW_REQUIRED`.
5. WHEN a payment request includes an idempotency key THEN the system SHALL treat the key as unique, and IF the same key is replayed THEN the system SHALL return the original result without creating a second payment or duplicate order effect.
6. WHEN a simulated payment succeeds THEN the system SHALL record an amount equal to the order total, confirm or decrement the reserved inventory exactly once, and advance the order state exactly once.
7. IF a simulated payment fails THEN the system SHALL NOT mark the order as paid and SHALL NOT permanently consume stock.
8. WHEN an order or payment state change is requested THEN the system SHALL validate the transition against the defined state machine in the domain layer and SHALL NOT accept a client-supplied status as authoritative.
9. WHEN a refund is simulated THEN the system SHALL record the refund without altering the historical prices captured on order items.
10. WHERE a high-value simulated purchase is configured to require additional assurance THEN the system SHALL support a step-up authentication challenge using a fresh password confirmation or an emailed one-time code.

### Requirement 13: Fulfilment

**User Story:** As a buyer and a seller, I want a clear handover process, so that both parties know when an order has been collected, delivered, or completed.

#### Acceptance Criteria

1. WHERE fulfilment is recorded THEN the system SHALL support the methods `PICKUP`, `LOCAL_DELIVERY`, `DIGITAL`, and `SERVICE_BOOKING`.
2. WHEN a seller order is paid THEN the system SHALL create a fulfilment record with an initial status and SHALL allow only the owning seller to advance it.
3. WHERE a pickup code is used THEN the system SHALL store only a hash of that code and SHALL NEVER store or return it in plaintext.
4. WHEN a buyer confirms receipt THEN the system SHALL record the confirmation time and SHALL use it as the trigger for review eligibility and simulated settlement release.
5. WHEN every seller order in a checkout reaches a completed state THEN the system SHALL transition the parent checkout order to `COMPLETED`.
6. WHERE some but not all seller orders are complete THEN the system SHALL represent the parent checkout order as partially fulfilled.
7. WHERE fulfilment notes are stored THEN the system SHALL discourage inclusion of unnecessary personal or address detail.

### Requirement 14: Verified-purchase reviews and reputation

**User Story:** As a buyer, I want to review a purchase I actually completed, so that ratings on the platform reflect genuine transactions.

#### Acceptance Criteria

1. WHEN a review is submitted THEN the system SHALL require that the reviewer is the buyer on a fulfilled order item.
2. WHERE reviews are stored THEN the system SHALL permit at most one review per purchased order item.
3. WHEN a review is submitted THEN the system SHALL require a product rating and a seller rating, each an integer between 1 and 5 inclusive.
4. WHEN review text is submitted THEN the system SHALL sanitise it, enforce a maximum length, and record a moderation status.
5. WHEN seller reputation is calculated THEN the system SHALL aggregate only published reviews.
6. WHERE a seller responds to a review THEN the system SHALL permit at most one response and SHALL record its timestamp.
7. IF review activity indicates a rating ring or self-review through a linked account THEN the system SHALL raise a fraud assessment for moderator review.
8. WHEN a review is hidden or removed by moderation THEN the system SHALL recalculate the affected reputation projections.

### Requirement 15: Community bulletin and notifications

**User Story:** As a community member, I want announcements and timely notifications, so that I stay informed about campus activity and my own transactions.

#### Acceptance Criteria

1. WHEN a bulletin post is created THEN the system SHALL require a post type, a title, and a body, and SHALL sanitise the body before storage or display.
2. WHERE a bulletin post targets a single institution THEN the system SHALL associate it with that institution, and WHERE it targets the wider community THEN the system SHALL allow the institution association to be absent.
3. WHERE bulletin posts carry an expiry THEN the system SHALL stop presenting them publicly after that time.
4. WHEN a notable event occurs, including order placement, payment outcome, fulfilment progress, verification decision, review receipt, or a security event THEN the system SHALL create a notification for the affected member.
5. WHERE notifications are delivered THEN the system SHALL support at least in-app and email channels and SHALL record a delivery status.
6. WHEN a notification is created as part of a business transaction THEN the system SHALL persist it in the same transaction and dispatch it asynchronously after commit, so that a delivery failure does not roll back the transaction.
7. WHERE notification content is composed THEN the system SHALL NOT include passwords, raw tokens, JWTs, or private verification document content.
8. WHEN a member reads a notification THEN the system SHALL record the read time.

### Requirement 16: Reporting, moderation, and fraud assessment

**User Story:** As a moderator, I want to act on reports and suspicious patterns with a recorded rationale, so that the marketplace stays safe and decisions remain accountable.

#### Acceptance Criteria

1. WHEN an authenticated member submits a report THEN the system SHALL record the target type, target identifier, and a reason from a defined set.
2. WHEN a moderator resolves a report THEN the system SHALL record the resolving actor, the outcome, and the resolution time.
3. WHERE moderation decisions are recorded THEN the system SHALL treat the moderation action history as append-only and SHALL require a reason for every action.
4. WHEN a moderator suspends an account, seller, listing, review, or post THEN the system SHALL immediately prevent the suspended resource from being publicly visible or transactable.
5. WHERE a suspension is temporary THEN the system SHALL record its expiry.
6. WHERE fraud detection is implemented for the MVP THEN the system SHALL use transparent documented rules and SHALL NOT be described as machine learning unless a trained model is actually implemented.
7. WHEN a fraud rule triggers THEN the system SHALL record a risk score between 0 and 100, a risk level, the specific rule codes that fired, and a decision of `ALLOW`, `CHALLENGE`, `REVIEW`, or `BLOCK`.
8. WHERE fraud rules are defined THEN the system SHALL include at minimum repeated login failures, rapid listing creation, duplicate image checksums, self-purchase attempts, repeated simulated payment failures, suspicious rating patterns, and unusual price changes.
9. WHEN a moderator reviews a fraud assessment THEN the system SHALL allow the outcome to be recorded as confirmed, a false positive, or escalated.
10. IF an automated rule would block a member THEN the system SHALL provide a moderator review path rather than a permanent unappealable block.

### Requirement 17: Audit trail

**User Story:** As the platform owner, I want a reliable record of security and administrative events, so that incidents can be investigated and privileged actions can be held accountable.

#### Acceptance Criteria

1. WHEN a security or administrative event occurs, including registration, verification, login outcome, session revocation, role change, verification decision, moderation action, and configuration change THEN the system SHALL record an audit event.
2. WHERE audit events are stored THEN the system SHALL treat them as append-only.
3. WHEN an audit event is recorded THEN the system SHALL include the acting account where one exists, a stable event type code, the target reference, the occurrence time, and a request correlation identifier.
4. WHERE audit metadata is recorded THEN the system SHALL include only allowlisted fields and SHALL NEVER record passwords, raw tokens, JWTs, cookies, or authorization headers.
5. WHERE source addresses are retained for security analysis THEN the system SHALL store them in a hashed form rather than in plaintext.
6. WHERE audit data is exposed THEN the system SHALL restrict access to authorised administrators.
7. WHERE high-volume security tables are stored, including login attempts and audit events THEN the system SHALL define retention and archival rules rather than retaining all rows indefinitely.

### Requirement 18: Database integrity and persistence conventions

**User Story:** As a developer, I want consistent enforced persistence rules, so that data stays correct under concurrency and the schema is reproducible for every team member.

#### Acceptance Criteria

1. WHERE business entities are persisted THEN the system SHALL use a UUID primary key, a creation timestamp, an update timestamp, and an optimistic-locking version field.
2. WHERE timestamps are stored THEN the system SHALL use UTC, and WHERE timestamps are returned by the API THEN the system SHALL format them as ISO-8601.
3. WHERE uniqueness is required THEN the system SHALL enforce it with a database constraint in addition to any service-level check, so that concurrent requests cannot both pass a pre-check.
4. WHERE the system enforces unique values THEN it SHALL include normalised account email, active institutional affiliation email, institutional email domain, approved business registration number, approved VAT number, listing slug, checkout and seller order numbers, cart item combination, review per order item, payment idempotency key, and refresh token hash.
5. WHERE bounded numeric values exist THEN the system SHALL apply check constraints for positive quantities, ratings between 1 and 5, risk scores between 0 and 100, and non-negative monetary amounts.
6. WHERE entities are related THEN the system SHALL define foreign key constraints for every owned relationship.
7. WHERE records carry financial, verification, moderation, or audit significance THEN the system SHALL preserve them through status transitions rather than deleting them.
8. WHERE soft deletion is used THEN the system SHALL apply it only to resources for which restoration is meaningful, such as listings and bulletin posts.
9. WHERE files are handled THEN the system SHALL store file metadata and an object-storage key in MySQL and SHALL NOT store image or document bytes in the database.
10. WHERE schema changes are made THEN the system SHALL express them as versioned migrations that reproduce the full schema on an empty MySQL database.
11. WHERE a deployed environment starts THEN the system SHALL validate the existing schema rather than allowing the ORM to mutate it automatically.
12. WHERE frequently queried collections are exposed THEN the system SHALL define supporting indexes for the status and date combinations used by listing, order, moderation, and notification queries.

### Requirement 19: API, transport, and browser security

**User Story:** As a security-conscious reviewer, I want the API hardened against common web attacks, so that the React client can integrate safely without exposing members to avoidable risk.

#### Acceptance Criteria

1. WHERE cross-origin access is configured THEN the system SHALL permit only explicitly configured frontend origins and SHALL NOT combine a wildcard origin with credentialed requests.
2. WHERE cross-origin access is configured THEN the system SHALL declare permitted methods and headers explicitly, including authorization, content type, and correlation or idempotency headers.
3. WHERE access-token authentication is used THEN the system SHALL treat the API as stateless and SHALL NOT rely on server-side HTTP sessions for it.
4. WHERE refresh tokens are delivered as cookies THEN the system SHALL apply a strict same-site policy and an anti-CSRF strategy appropriate to the deployed topology for state-changing cookie-authenticated endpoints.
5. WHEN an error occurs THEN the system SHALL return a consistent structured problem response and SHALL NOT expose stack traces, SQL, internal class names, or configuration values.
6. WHEN user-supplied content is stored or returned THEN the system SHALL sanitise it server-side rather than relying on frontend escaping to make arbitrary HTML safe.
7. WHERE abuse is possible THEN the system SHALL rate-limit login, password reset, verification resend, listing creation, review submission, report submission, and simulated payment attempts.
8. WHERE persistence-layer objects exist THEN the system SHALL NOT return JPA entities directly from controllers and SHALL expose purpose-built response objects instead.
9. WHERE requests are logged THEN the system SHALL NOT log authorization headers, cookies, passwords, raw tokens, or uploaded document contents.
10. WHERE the application is deployed THEN the system SHALL serve traffic over HTTPS and SHALL set appropriate security response headers.
11. WHERE database queries are constructed THEN the system SHALL use parameterised queries or the ORM's binding mechanisms and SHALL NOT build queries by string concatenation of user input.

### Requirement 20: Configuration, secrets, and environment separation

**User Story:** As a team member, I want configuration and secrets externalised, so that credentials are not committed to the repository and each environment behaves predictably.

#### Acceptance Criteria

1. WHERE the datasource is configured THEN the system SHALL read the URL, username, and password from environment variables or a secret manager and SHALL NOT contain a plaintext database password in a committed file.
2. WHEN this spec is implemented THEN the system SHALL replace the currently committed MySQL credentials, and the team SHALL rotate that password if it is a real credential.
3. WHERE JWT signing material is configured THEN the system SHALL keep the private signing key outside source control and SHALL support key rotation through a key identifier in the token header.
4. WHERE token lifetimes, issuer, and audience are configured THEN the system SHALL read them from configuration rather than hard-coding them.
5. IF required security configuration is missing or invalid at startup THEN the system SHALL fail fast with a clear error rather than starting in an insecure default state.
6. WHERE environments differ THEN the system SHALL provide separate profiles for local development, automated tests, and production and SHALL NOT share one credential set across them.
7. WHERE automated tests run THEN the system SHALL use an isolated test datasource and fixed test keys and SHALL NOT depend on a developer's local production-like database.
8. WHERE dependencies are declared THEN the system SHALL add JWT encoding and decoding support, request validation, and a database migration tool, and SHALL retain the OAuth2 client dependency only if external institutional sign-in is deliberately adopted.
9. WHERE the project structure is finalised THEN the system SHALL correct the misspelled `until` package to `util` before other code depends on it.
10. WHERE secrets could be committed THEN the repository SHALL exclude real database credentials, JWT private keys, mail provider passwords, and cloud credentials.

### Requirement 21: Delivery sequence and demonstrable acceptance

**User Story:** As a project team working to an academic deadline, I want the backend delivered in verifiable phases, so that a complete secure marketplace exists before optional engagement features are attempted.

#### Acceptance Criteria

1. WHERE implementation is sequenced THEN the system SHALL deliver the security and identity foundation first, comprising configuration, migrations, accounts, roles, tokens, sessions, and institutional verification.
2. WHERE implementation is sequenced THEN the system SHALL deliver seller onboarding, business verification, and the catalog second.
3. WHERE implementation is sequenced THEN the system SHALL deliver cart, checkout, simulated payment, fulfilment, and notifications third.
4. WHERE implementation is sequenced THEN the system SHALL deliver reviews, bulletin, fraud assessment, and moderation dashboards fourth.
5. WHERE optional features are considered THEN the system SHALL defer wishlists, promotions, referrals, loyalty points, direct messaging, comments, polls, caching, service extraction, automated CIPC integration, and machine-learning fraud detection until the core marketplace is complete.
6. WHEN the identity foundation is claimed complete THEN a community member SHALL be able to register, verify email, log in, refresh with rotation, and log out, and an unverified or suspended account SHALL be unable to obtain new tokens.
7. WHEN the verification foundation is claimed complete THEN a supported university mailbox SHALL produce a verified affiliation badge without granting a privileged role, and an unconfirmed institutional domain SHALL produce no automatic badge.
8. WHEN the commerce backend is claimed complete THEN concurrent checkout attempts SHALL NOT oversell stock, a buyer SHALL NOT purchase their own listing, and order snapshots SHALL remain unchanged when the underlying listing or address is later edited.
9. WHEN any phase is claimed complete THEN the schema SHALL be reproducible from migrations on an empty database and no endpoint SHALL expose entities, password hashes, raw tokens, or private verification evidence.
10. WHERE this spec informs assessed academic work THEN the team SHALL review and approve the model, record its own decisions and testing evidence, cite sources in submitted deliverables, comply with institutional rules on AI assistance and plagiarism, and SHALL NOT claim research, testing, or stakeholder engagement that was not performed.
