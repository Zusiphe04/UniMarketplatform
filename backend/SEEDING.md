# UniMarket Manual Seed Workflows

UniMarket is seeded through its real REST API rather than direct SQL inserts. The requests are separated by feature so they are easy to read, run, and debug in IntelliJ IDEA. The two frontend-focused workflows, `00_frontend_platform_users.http` and `03_frontend_catalogue.http`, use IntelliJ HTTP Client response handlers to validate and automatically capture generated values. The legacy numbered workflows remain manual `PASTE_*` workflows.

## Seed files

Run the files in this order:

| Order | File | Purpose |
|---:|---|---|
| 0 (frontend roles) | [`00_frontend_platform_users.http`](http/seeding/00_frontend_platform_users.http) | Auto-captured admin, verified seller, student, faculty, resident, buyer, and moderator accounts for role-aware frontend flows |
| 1 | [`01_users_and_auth.http`](http/seeding/01_users_and_auth.http) | Administrator login; seller, student, faculty, resident, and buyer accounts; profiles and personas |
| 2 | [`02_vendor_approval.http`](http/seeding/02_vendor_approval.http) | Vendor application, administrator approval, and fresh seller JWT |
| 3 | [`03_product_listings.http`](http/seeding/03_product_listings.http) | Textbook/laptop listings, image URLs, publication, and public reads |
| 3a (optional) | [`03_frontend_catalogue.http`](http/seeding/03_frontend_catalogue.http) | Auto-captured full 24-item frontend catalogue using the supplied Vite-served product and service images |
| 4 | [`04_reports_moderation.http`](http/seeding/04_reports_moderation.http) | Product report and moderation while the laptop is still publicly visible |
| 5 | [`05_events_bulletin.http`](http/seeding/05_events_bulletin.http) | Resident-created campus event and public bulletin reads |
| 6 | [`06_cart_checkout.http`](http/seeding/06_cart_checkout.http) | Cart, pickup checkout, delivery checkout, and three pending order UUIDs |
| 7 | [`07_payments.http`](http/seeding/07_payments.http) | Declined/retried low payment and two high-value held payments |
| 8 | [`08_reviews_feedback.http`](http/seeding/08_reviews_feedback.http) | Verified review, public feedback, and seller response |
| 9 | [`09_escrow.http`](http/seeding/09_escrow.http) | Buyer release plus separate dispute/moderator refund |
| 10 | [`10_engagement.http`](http/seeding/10_engagement.http) | Points, badges, ledger, and leaderboards |
| 11 | [`11_notifications_sessions.http`](http/seeding/11_notifications_sessions.http) | Notifications, refresh rotation, session revocation, password restore, and logout-all |

After all HTTP workflows, run [`scripts/verify-seed-data.sql`](scripts/verify-seed-data.sql) in MySQL Workbench. It uses read-only `SELECT` queries to inspect all 22 mapped tables. The script also includes a separate 24-item image-coverage query for the optional frontend catalogue.

## 1. Prerequisites

- MySQL running locally.
- Java 21 available through `JAVA_HOME` for the Maven wrapper.
- IntelliJ IDEA HTTP Client.
- A fresh local `unimarket` schema. Fixed emails and unique records mean the full sequence is intentionally not rerunnable on the same seed data.
- For `03_frontend_catalogue.http`, the Vite frontend running at `http://localhost:5173` so its localhost image URLs are reachable while browsing the catalogue.

The repository's local datasource defaults are:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/unimarket?createDatabaseIfNotExist=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Other machines can override them without editing source:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "another-local-password"
$env:DB_URL = "jdbc:mysql://localhost:3306/unimarket?createDatabaseIfNotExist=true&serverTimezone=UTC"
```

## 2. Configure JWT signing and bootstrap administrator

Run this in PowerShell before starting the API:

```powershell
$bytes = New-Object byte[] 48
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $random.GetBytes($bytes) } finally { $random.Dispose() }
$env:UNIMARKET_JWT_SECRET = [Convert]::ToBase64String($bytes)

$env:UNIMARKET_BOOTSTRAP_ADMIN_ENABLED = "true"
$env:UNIMARKET_ADMIN_EMAIL = "admin.seed@unimarket.local"
$env:UNIMARKET_ADMIN_PASSWORD = "AdminSeed@12345"
$env:UNIMARKET_ADMIN_FIRST_NAME = "UniMarket"
$env:UNIMARKET_ADMIN_LAST_NAME = "Administrator"
```

The administrator is created only through this opt-in bootstrap. Public administrator registration is intentionally unavailable.

## 3. Start the API

Run this long-lived command manually in an IntelliJ terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

The split request files use:

```text
http://localhost:8080
```

Port `8081` is not configured unless `server.port` is explicitly overridden.

## 4. Automatic captures and legacy `PASTE_*` variables

The two frontend-focused files are safe to run top-to-bottom without manually editing generated values:

- `00_frontend_platform_users.http` asserts and captures the required access tokens and account IDs with `client.test` and `client.global.set`.
- `03_frontend_catalogue.http` logs in the already-approved platform seller, captures its access token, and captures each of the 24 product IDs before the matching image and publish requests.

Run each frontend-focused file in one IntelliJ HTTP Client session so its `client.global` values remain available to subsequent requests. The files remain intentionally non-idempotent: running registrations or product creates again against the same data will fail or create duplicates.

The legacy numbered files `01_users_and_auth.http` through `11_notifications_sessions.http`, including `03_product_listings.http`, continue to use visible manual variables such as:

```http
@baseUrl = http://localhost:8080
@buyerToken = PASTE_BUYER_JWT_HERE
@lowOrderId = PASTE_TEXTBOOK_PENDING_ORDER_UUID_HERE
```

For those legacy files, run one request at a time. When a response generates a value needed later:

1. Copy `accessToken` from a login response into the relevant `@...Token` variable.
2. Copy `accountId` from registration when an account UUID is requested.
3. Copy product, order, payment, review, report, event, notification, and session `id` values into the matching variables.
4. For file 07, copy the declined SnapScan response `id` into `@declinedPaymentId` and the successful PayFast retry response `id` into `@successfulPaymentId`.
5. Use an order response's UUID `id`, not its human-readable `UM-*` reference.
6. Save the file and run the dependent request.

Access JWTs expire after 15 minutes. Rerun the relevant login when a token may be old. In an auto-capture file, its response handler replaces the corresponding global value; in a legacy file, paste the fresh `accessToken` manually. Seller requests after approval must use a fresh seller login so the JWT contains `SELLER`.

## 5. Seed identities and academic persona fields

| Identity | Email | Academic password | Result |
|---|---|---|---|
| Bootstrap administrator | `admin.seed@unimarket.local` | `AdminSeed@12345` | `BUYER`, `SELLER`, `MODERATOR`, `ADMIN` |
| Approved seller | `seller.seed@unimarket.local` | `SellerSeed@12345` | `BUYER`, `SELLER`, `RESIDENT`, verified `VENDOR` |
| Student | `240000001@mycput.ac.za` | `StudentSeed@12345` | `BUYER`, verified `STUDENT` |
| Faculty | `faculty.seed@cput.ac.za` | `FacultySeed@12345` | `BUYER`, verified `FACULTY` |
| Resident/event organizer | `resident.seed@unimarket.local` | `ResidentSeed@12345` | `BUYER`, `RESIDENT` |
| Customer/buyer | `buyer.seed@unimarket.local` | `BuyerSeed@12345` | `BUYER` |

These are generic local-development credentials, not production credentials.

Academic persona declarations can additionally persist `qualification` (`DIPLOMA`, `BACHELORS_DEGREE`, `ADVANCED_DIPLOMA`, `MASTERS`, `HONORS`, or `PHD`) and a student `yearOfStudy` from 1 through 6. `00_frontend_platform_users.http` seeds a second-year bachelor's student and a PhD-qualified faculty member. Both columns are nullable for compatibility with older requests. In the current local-development setup, Hibernate `ddl-auto=update` adds `qualification` and `year_of_study` to `user_persona_assignment` when the updated API starts.

## 6. Expected final outcomes

After all 11 legacy files are completed in order:

- the seller is verified and has a fresh JWT containing `SELLER`;
- the textbook is `PUBLISHED` with quantity `4`;
- the laptop is `SOLD_OUT` with quantity `0`;
- two reachable image URLs exist in `product_image` and public `product.images` responses;
- the textbook order is `PAID` after one declined attempt and one successful retry;
- the first laptop order and escrow are `RELEASED`;
- the second laptop order and escrow are `REFUNDED` after a dispute;
- the buyer has one verified review and the seller has one response;
- the product report is `RESOLVED` with `ESCALATED` resolution/action;
- buyer points total `23` and seller points total `24`;
- the event, notifications, badges, point ledger, and session audit rows exist;
- one textbook cart row remains for direct `cart_item` verification;
- no active buyer refresh session remains after logout-all.

After the optional frontend catalogue file, the platform seller should have 24 products, 24 products with images, 24 primary images, and 24 image rows.

## 7. Correct payment contract

UniMarket does not expose `/payments/card` or `/payments/wallet`. Payments are credential-free academic simulations:

```http
@baseUrl = http://localhost:8080
@buyerToken = PASTE_BUYER_JWT_HERE
@orderId = PASTE_PENDING_ORDER_UUID_HERE

POST {{baseUrl}}/api/v1/payments/simulate
Authorization: Bearer {{buyerToken}}
Idempotency-Key: seed-payment-{{orderId}}
Content-Type: application/json

{
  "orderId": "{{orderId}}",
  "paymentOption": "VISA",
  "scenario": "SUCCESS"
}
```

Valid options are `PAYFAST`, `SNAPSCAN`, `PAYSHAP`, `VISA`, and `MASTERCARD`. A delivery address belongs to `POST /api/v1/checkout`. The payment endpoint rejects card numbers, CVV, payment tokens, wallet tokens, bank passwords, OTPs, and unknown fields.

## Product categories and structured specifications

Seller product requests use these canonical category values: `BOOKS`, `TECH`, `CLOTHING`, `ROOM_AND_HOME`, `SERVICE`, and `OTHER`. `SERVICE` requires `NOT_APPLICABLE` condition; every physical category requires a physical condition.

Optional structured fields are accepted only where they are meaningful:

| Category | Supported optional fields |
|---|---|
| `TECH` | `brand`, `model`, `storage`, `memory`, `processor`, `screenSize`, `color` |
| `CLOTHING` | `brand`, `size`, `color` |
| `ROOM_AND_HOME` | `brand`, `model`, `size`, `color` |
| `OTHER` | `brand`, `model`, `size`, `color` |
| `BOOKS`, `SERVICE` | None of the structured fields above |

Blank optional values are stored as `null`; incompatible fields are rejected rather than silently attached to the wrong category. Existing databases remain compatible because the canonical API categories map to the original persisted enum values (`TECH` to `ELECTRONICS`, `CLOTHING` to `FASHION`, `ROOM_AND_HOME` to `HOME`, and `SERVICE` to `SERVICES`). The specification columns are nullable additions, so existing product rows require no fabricated values.

Example technology payload fragment:

```json
{
  "category": "TECH",
  "condition": "LIKE_NEW",
  "brand": "Lenovo",
  "model": "ThinkPad",
  "storage": "512 GB SSD",
  "memory": "16 GB RAM",
  "processor": "Intel Core i7",
  "screenSize": "14 inches",
  "color": "Silver"
}
```

## 8. Product image URL workflow

The backend accepts both permanent external image URLs and validated multipart uploads. Each `product_image` row stores URL metadata—URL, alt text, display order, and primary flag—while uploaded binary files are served from the configured media directory.

All 24 image files referenced by `03_frontend_catalogue.http` exist under the repository's `frontend/public/images` tree. Vite exposes that tree from `http://localhost:5173`, so Vite and that local origin must be reachable from the browser while browsing seeded products. Stopping Vite does not remove the database rows, but the stored localhost URLs will no longer render. For deployed environments, replace `@frontendUrl` with a reachable public frontend or CDN origin before seeding.

For other frontend-created products:

1. Upload the binary file to a storage provider such as Firebase Storage, Cloudinary, S3, or Supabase Storage.
2. Obtain a permanent public HTTPS delivery URL.
3. Save the URL metadata through the seller endpoint:

```http
POST http://localhost:8080/api/v1/seller/products/{{productId}}/images
Authorization: Bearer {{sellerToken}}
Content-Type: application/json

{
  "imageUrl": "https://cdn.example.org/products/product-photo.jpg",
  "altText": "Accessible description of the product photo",
  "displayOrder": 0,
  "primary": true
}
```

The URL is stored in `product_image.image_url`. Public product list/detail responses return ordered `images` arrays suitable for React `<img>` elements.

## 9. Verify MySQL persistence

Open [`scripts/verify-seed-data.sql`](scripts/verify-seed-data.sql) in MySQL Workbench after completing file 11. The script contains no data mutation and verifies:

- all 22 mapped tables and row counts;
- accounts, profiles, roles, personas, academic qualification/year fields, and vendor approval;
- products, distinct image URLs, inventory, and retained cart data;
- three orders, four payment attempts, and two escrows;
- review/response, event, points, badges, report, notifications, and sessions;
- expected totals in a final one-row acceptance checklist;
- optional frontend-catalogue image coverage for `seller.platform@unimarket.local` (expected `24/24/24/24` after file 03).

## Frontend integration notes

- Send access tokens as `Authorization: Bearer <token>`.
- Use `credentials: "include"` for browser login, refresh, and logout so the HttpOnly refresh cookie is sent.
- UUIDs are JSON strings, timestamps are UTC ISO-8601 values, and money is calculated by the server in ZAR.
- Spring page responses contain `content`, `number`, `size`, `totalElements`, and `totalPages`.
- API failures use `application/problem+json`.
