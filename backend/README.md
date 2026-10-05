# UniMarket Community Store Backend

UniMarket is a Java 21/Spring Boot/MySQL API for a campus-community marketplace consumed by a React frontend. It supports multi-role accounts, university/community personas, approved sellers, products and media, carts and orders, simulated South African payments, verified reviews and seller responses, bulletin events, notifications, loyalty points and badges, buyer/seller leaderboards, simulated high-value escrow, and suspicious-activity moderation.

## Architecture

- Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, and MySQL
- HS256 JWT access tokens plus rotated opaque refresh sessions
- Exactly four configuration classes: `JwtConfig`, `SecurityConfig`, `CorsConfig`, and `PasswordEncoderConfig`
- Flat technical packages: `domain`, `domain.enums`, `factory`, `repository`, `request`, `response`, `service`, `service.impl`, and `controller`
- Builder entities, validated factories, repository abstractions/adapters, transactional services, and REST controllers
- React-compatible JSON and RFC Problem Detail errors

Registration creates an immediately active account with `BUYER`. Roles authorize behavior; personas describe identity and trust. Approved vendor profiles receive `SELLER`. The opt-in bootstrap administrator receives `BUYER`, `MODERATOR`, and `ADMIN`.

## Implemented API areas

| Area | Main paths |
|---|---|
| Authentication/session | `/api/v1/auth/**` |
| Account/personas/vendor profile | `/api/v1/account/**` |
| Administration | `/api/v1/admin/**` |
| Public products | `/api/v1/products/**` |
| Seller products/orders | `/api/v1/seller/**` |
| Cart, checkout, buyer orders | `/api/v1/cart/**`, `/api/v1/checkout`, `/api/v1/orders/**` |
| Simulated payments | `/api/v1/payments/**` |
| Verified reviews and seller responses | `/api/v1/reviews/**` |
| Bulletin announcements/events | `/api/v1/bulletin/**` |
| Notifications | `/api/v1/notifications/**` |
| Loyalty profile and leaderboards | `/api/v1/engagement/**`, `/api/v1/leaderboards/**` |
| Simulated escrow | `/api/v1/escrow/**`, `/api/v1/moderation/escrow/**` |
| Reports and moderation queue | `/api/v1/reports/**`, `/api/v1/moderation/reports/**` |

Payment simulation accepts one of `PAYFAST`, `SNAPSCAN`, `PAYSHAP`, `VISA`, or `MASTERCARD` and an allowlisted outcome. It never accepts card numbers, CVVs, passwords, bank credentials, or OTPs. Successful low-value payments move orders to `PAID`; orders at or above the configured threshold move to simulated escrow `HELD` until buyer confirmation or moderator dispute resolution.

## Build

The Maven wrapper pins the project's Maven version, so a global Maven installation is not required. Run the wrapper with JDK 21:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21" # Adjust if JDK 21 is installed elsewhere.
.\mvnw.cmd clean verify
```

The executable Spring Boot JAR is written to `target/UniMarket-0.0.1-SNAPSHOT.jar`.

The application requires database credentials from the environment; do not commit local passwords:

```powershell
$env:DB_USERNAME = "unimarket_app"
$env:DB_PASSWORD = "Use-a-local-secret-here"
$env:DB_URL = "jdbc:mysql://localhost:3306/unimarket?createDatabaseIfNotExist=true&serverTimezone=UTC"
```

A Base64-encoded JWT secret is also mandatory:

```powershell
$bytes = New-Object byte[] 48
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $random.GetBytes($bytes) } finally { $random.Dispose() }
$env:UNIMARKET_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

## Seed data and frontend integration

Follow **[SEEDING.md](SEEDING.md)** and run the numbered IntelliJ files under [`http/seeding/`](http/seeding/) in order, then execute the read-only [`scripts/verify-seed-data.sql`](scripts/verify-seed-data.sql) in MySQL Workbench. The manual workflow is separated into users/authentication, vendor approval, product listings/images, reports/moderation, events, cart/checkout, payments, reviews/feedback, escrow, engagement, and notifications/sessions.

Each file uses visible `@... = PASTE_*` variables for tokens and generated UUIDs. There are no hidden response scripts or shared environment files. The requests target the application's default `http://localhost:8080` origin and exercise:

- bootstrap administrator/moderator and refresh-session lifecycle;
- approved seller/vendor plus student, faculty, resident, and buyer personas;
- low- and high-value products with image metadata and retained cart data;
- a published community event and concrete checkout delivery address;
- declined and successful South African payment simulations;
- verified review, seller response, points, badges, and both leaderboards;
- one high-value escrow released by buyer confirmation;
- a separate high-value escrow disputed and refunded by moderation;
- suspicious-activity report, moderator queue, and resolution notification;
- notification read state, refresh rotation, targeted revocation, password restore, and logout-all.

Product image URLs are persisted in `product_image.image_url`, and public product list/detail responses return ordered `images` arrays for React rendering. For user-created images, upload the binary to a storage provider first and POST its permanent public HTTPS delivery URL; see [SEEDING.md](SEEDING.md#8-product-image-url-workflow).

The product API exposes the canonical listing categories `BOOKS`, `TECH`, `CLOTHING`, `ROOM_AND_HOME`, `SERVICE`, and `OTHER`. Technology listings can persist brand, model, storage, memory, processor, screen size, and color; clothing persists brand, size, and color. These are nullable additive columns. Canonical API values map to the original persisted category enum strings, preserving existing database rows while new clients use the clearer category names.

## Development notes

- `spring.jpa.hibernate.ddl-auto=update` is a local-development convenience. Production should use versioned migrations and `validate`.
- Escrow and payment providers are simulations only; no real funds are held, captured, released, or refunded.
- Product images support permanent external HTTP(S) URL metadata and validated multipart uploads. Render deployments must keep `UNIMARKET_MEDIA_UPLOAD_DIR` on a persistent disk or replace local storage with object storage.
- A simulated escrow refund intentionally does not restock inventory automatically.
- CORS origins come from `FRONTEND_ORIGINS` and must be explicit when credentials are enabled.


## Marketplace operational lifecycle

Successful payments create an operational fulfilment record for each seller-owned order item. Sellers can accept an item, mark it ready for collection with instructions, or mark it dispatched with a tracking reference. Buyers can then confirm receipt per item. This lifecycle is intentionally separate from the order-wide payment and simulated-escrow state because a single checkout may include multiple sellers.

Public product responses now contain a deliberately narrow `seller` summary (`id`, `displayName`) so catalogue and detail views identify who listed an item without exposing profile contact information.

## Community event registration

Published `EVENT` bulletin posts may specify an optional positive attendee capacity. Authenticated members can register, are confirmed while places remain, or join a FIFO waitlist when full. Cancelling a confirmed registration promotes the longest-waiting active registration atomically. Event hosts can see attendee and waitlist status, and can cancel an event with a reason; affected attendees are notified and their registrations are preserved as `EVENT_CANCELLED` for auditability.

| Event path | Purpose |
|---|---|
| `POST /api/v1/events/{eventId}/registrations` | Register or join the waitlist |
| `GET` / `DELETE /api/v1/events/{eventId}/registrations/me` | Read or cancel the caller's registration |
| `GET /api/v1/events/{eventId}/attendees` | Host attendee and waitlist view |
| `PATCH /api/v1/events/{eventId}/cancel` | Host event cancellation with a reason |

The added SQL migration in `src/main/resources/db/migration/` is an explicit schema extension for a Flyway-baselined deployment. Local development currently retains Hibernate schema updates for compatibility with the existing project database; baseline the current schema and enable Flyway before changing a shared or production-like environment to `ddl-auto=validate`.

## Checkout reliability and token invalidation

`POST /api/v1/checkout` requires an `Idempotency-Key` request header (1–120 trimmed characters). A retry with the same buyer, key, fulfillment method, and exact submitted delivery-address value returns the order created by the first request, retaining the established `201 Created` response. Reusing a key with different checkout details returns a validation error. The key and a SHA-256 request fingerprint are persisted on `marketplace_order` under a buyer/key unique constraint; `V2__checkout_idempotency_and_cart_constraints.sql` is the corresponding extension for a Flyway-baselined deployment.

Checkout pessimistically locks its cart-row snapshot and removes only those rows after the order is created, so a later-added cart row is not accidentally deleted. Checkout validates availability but deliberately **does not reserve or decrement inventory**. Stock allocation remains at successful payment, preserving the existing payment-time behavior and avoiding a partial reservation system.

Each bearer-token request now validates its `sid` token family against an active, non-revoked server session and confirms the account can still authenticate. Removing a role revokes all of that account’s sessions immediately, so access tokens containing the removed role are rejected on their next use. Newly granted roles remain effective after the next login or refresh, as before.
