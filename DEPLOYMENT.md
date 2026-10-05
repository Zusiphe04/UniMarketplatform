# Deploy Community Store to TiDB Cloud, Render, and Vercel

This repository is prepared for the following production topology:

- **TiDB Cloud** stores application data through its MySQL-compatible JDBC endpoint.
- **Render** builds `backend/Dockerfile`, runs the Spring API, and persists uploads on a mounted disk.
- **Vercel** builds the Vite frontend in `frontend/` and routes React SPA URLs to `index.html`.
- The frontend splash probes `GET /actuator/health`; it reaches 100% only when the API and database report `UP`.

## 1. Create the TiDB Cloud database

1. Create a TiDB Cloud Starter, Essential, or Dedicated cluster.
2. Create a database named `unimarket` and an application user with access only to that database.
3. In TiDB Cloud, open **Connect**, select a public connection, and copy the host, port, username, and password.
4. Use TLS verification. TiDB is MySQL-compatible and supports MySQL Connector/J ([TiDB JDBC guide](https://docs.pingcap.com/tidbcloud/dev-guide-sample-application-java-jdbc)); secure TiDB Cloud connections should use TLS ([TiDB TLS guide](https://docs.pingcap.com/tidbcloud/secure-connections-to-serverless-clusters)).

Example Render variables—replace every placeholder with the values from TiDB's Connect dialog:

```text
DB_URL=jdbc:mysql://<tidb-host>:4000/unimarket?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&serverTimezone=UTC
DB_USERNAME=<tidb-user>
DB_PASSWORD=<tidb-password>
```

The first deployment uses `JPA_DDL_AUTO=update` because this repository's existing SQL migrations are incremental and not a create-from-empty baseline. After the first successful deployment and schema verification, change it to `validate`. Do not enable Flyway against a fresh database until a complete baseline migration exists.

## 2. Create the Render backend

`render.yaml` defines a Docker web service because Java is not one of Render's listed native runtimes; Render supports Dockerfile builds ([Render Docker](https://render.com/docs/docker/)). Render health checks use the configured HTTP path ([Render health checks](https://render.com/docs/health-checks)).

1. Push the repository to GitHub.
2. In Render, choose **New > Blueprint** and select the repository.
3. Render reads the root `render.yaml` and builds `backend/Dockerfile`.
4. Enter all variables marked `sync: false`:

```text
DB_URL=<TiDB JDBC URL above>
DB_USERNAME=<TiDB username>
DB_PASSWORD=<TiDB password>
UNIMARKET_JWT_SECRET=<Base64 secret generated below>
JWT_ISSUER=https://<your-render-service>.onrender.com
FRONTEND_ORIGINS=https://<your-vercel-project>.vercel.app
```

Generate a Base64 JWT secret in PowerShell:

```powershell
$bytes = New-Object byte[] 48
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $random.GetBytes($bytes) } finally { $random.Dispose() }
[Convert]::ToBase64String($bytes)
```

The Blueprint uses a Starter instance and a 1 GB persistent disk at `/var/data/uploads`. Render's normal filesystem is ephemeral, so files disappear after restart/redeploy without a disk ([Render persistent disks](https://render.com/docs/disks)). A disk belongs to one service instance; use S3-compatible object storage before horizontal scaling.

After deployment, verify:

```text
https://<your-render-service>.onrender.com/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## 3. Create the Vercel frontend

Vercel supports Vite and exposes build-time variables with the `VITE_` prefix ([Vite on Vercel](https://vercel.com/docs/frameworks/vite)). The included `frontend/vercel.json` provides the SPA rewrite; Vercel rewrites preserve the visible browser URL while serving another destination ([Vercel rewrites](https://vercel.com/docs/edge-network/rewrites)).

1. In Vercel, import the same GitHub repository.
2. Set **Root Directory** to `frontend`.
3. Framework preset: **Vite**.
4. Build command: `npm run build`.
5. Output directory: `dist`.
6. Add these Production and Preview variables:

```text
VITE_API_BASE_URL=https://<your-render-service>.onrender.com
VITE_PUBLIC_APP_URL=https://<your-vercel-project>.vercel.app
VITE_STARTUP_MAX_WAIT_MS=90000
```

7. Deploy the frontend.
8. Return to Render and ensure `FRONTEND_ORIGINS` exactly matches the deployed Vercel origin. Add comma-separated exact origins if you also have a custom frontend domain.

## 4. Authentication domain recommendation

The default `vercel.app` frontend and `onrender.com` API are cross-site. This configuration therefore uses `Secure; SameSite=None` for the refresh cookie. Some browsers block third-party cookies regardless of SameSite.

For reliable production sessions, use sibling custom domains:

```text
app.example.com  -> Vercel
api.example.com  -> Render
```

Then set:

```text
VITE_API_BASE_URL=https://api.example.com
VITE_PUBLIC_APP_URL=https://app.example.com
FRONTEND_ORIGINS=https://app.example.com
JWT_ISSUER=https://api.example.com
REFRESH_COOKIE_SAME_SITE=Strict
```

Alternatively, configure a Vercel external rewrite for `/api`, `/actuator`, and `/uploads` after the final Render URL is known, leave `VITE_API_BASE_URL` blank, and test all Set-Cookie behavior in staging.

## 5. Splash and cold-start behavior

`ApiStartupGate` mounts before `AuthProvider`, so authentication and cart API calls do not race a sleeping Render service.

- Four orange squares animate as the Community Store mark.
- Progress advances toward 95% while `/actuator/health` is unavailable.
- It reaches 100% only after the health response reports `UP`.
- It retries with bounded exponential delays for up to `VITE_STARTUP_MAX_WAIT_MS`.
- After the timeout, the user sees a retry button instead of an infinite spinner.
- Reduced-motion preferences disable the spinning and fade animations.

## 6. Post-deploy verification

Run these checks from a browser and API client:

1. Open the Vercel root and a direct route such as `/marketplace`; both must render.
2. Confirm the splash stays visible while Render is sleeping and exits after health reports `UP`.
3. Register and sign in; refresh the browser and confirm the session is restored.
4. Load marketplace products and images.
5. As a buyer, add another seller's product, check out, and run a simulated payment.
6. As an approved vendor, verify buyer/cart/community routes are denied, edit an owned listing, and confirm the product notification appears.
7. Upload an image, redeploy Render, and confirm it remains available from the persistent disk.
8. Confirm Render's health status remains green while connected to TiDB.

## 7. Local development

Copy each example file without committing the result:

```powershell
Copy-Item backend\.env.example backend\.env
Copy-Item frontend\.env.example frontend\.env.local
```

Start the backend in IntelliJ or with the Maven wrapper, then run `npm run dev` in `frontend`. Vite proxies both `/api` and `/actuator` to `http://localhost:8080`, so the startup splash works locally.

Content was rephrased for compliance with licensing restrictions.
