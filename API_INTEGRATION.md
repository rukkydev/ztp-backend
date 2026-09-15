# ZTP Backend API Integration Guide

**Target Audience**: Frontend Engineering  
**Backend Environment**: Render Cloud (`https://ztp-backend.onrender.com`) & Local Development  
**Backend Framework**: Spring Boot 4.1.0, Java 25, Spring Security, Spring Session JDBC, Aiven MySQL  
**Repository**: [rukkydev/ztp-backend](https://github.com/rukkydev/ztp-backend)

---

## 1. Environment & Network Configuration

### API Base URLs
* **Production**: `https://ztp-backend.onrender.com`
* **Local Development**: `http://localhost:8080`

### Required Frontend Environment Variable
This project uses **Vite** as its build tool and dev server. All client-exposed environment variables **must** use the `VITE_` prefix (do not use `NEXT_PUBLIC_` or `REACT_APP_`).

Define in `.env` (or hosting provider environment settings, e.g., Vercel, Netlify):

```bash
# Production (.env.production or deployment dashboard)
VITE_API_URL=https://ztp-backend.onrender.com

# Local Development (.env.local)
VITE_API_URL=http://localhost:8080
```

> [!IMPORTANT]
> **Centralized Client Usage**: Never hardcode `http://localhost:8080`, `127.0.0.1`, or `https://ztp-backend.onrender.com` in individual page scripts or components. All network requests must route through your centralized `api-client.js` utilizing `import.meta.env.VITE_API_URL`.

### Known Cloud Cold-Start Behavior (Render Free Tier)
Render's free tier spins down the backend container after ~15 minutes of inactivity.
* **Initial Request Latency**: The very first request after an idle period will experience a **~50 second cold start** while Render provisions the container, initializes Java 25, connects to Aiven MySQL over SSL, and runs schema verification.
* **Frontend Handling**: Ensure `DEFAULT_TIMEOUT_MS` in `api-client.js` is set to at least `60000` (60s) on initial health/ping requests or display a user-friendly loading state ("Platform waking up...") rather than immediately throwing a network error. Once active, response times return to normal (<100ms).

---

## 2. Authentication & Session Architecture

> [!CAUTION]
> **ZTP DOES NOT USE JWT OR BEARER TOKENS.**  
> Do not store tokens in `localStorage`, `sessionStorage`, or attach `Authorization: Bearer <token>` headers. The backend will ignore or reject them.

### Real Authentication Model
1. **Server-Side Persisted Sessions**:
   * Sessions are managed via **Spring Session JDBC** and backed by dedicated database tables (`SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`) on Aiven MySQL.
   * On successful authentication, the backend issues an HTTP cookie named `SESSION`.
2. **Account Creation Policy**:
   * **No self-service registration endpoint exists**. ZTP enforces strict enterprise Zero Trust onboarding. Accounts are provisioned exclusively by administrators via the Admin Users module (`POST /api/admin/users`).

---

## 3. The Cross-Origin Cookie Requirements

Because the frontend and backend live on separate domains (e.g. `https://your-app.vercel.app` vs. `https://ztp-backend.onrender.com`), the session and CSRF cookies are **cross-site (third-party)** cookies.

### Backend Cookie Attributes (Configured & Enforced)
Both the session cookie and CSRF cookie are emitted with:
* `SameSite=None`
* `Secure=true`
* `HttpOnly=true` (for `SESSION`) / `HttpOnly=false` (for `XSRF-TOKEN` so JavaScript can read it)
* `Path=/`

### Frontend Invocations (`credentials: 'include'`)
On the frontend, **every** network request made via `fetch()` or `axios` **must include credentials**:

```javascript
// Native fetch in api-client.js
fetch(url, {
  method: 'POST',
  headers: requestHeaders,
  credentials: 'include', // MANDATORY: allows browser to store and transmit cross-site cookies
  body: JSON.stringify(payload)
});

// If using Axios
axios.defaults.withCredentials = true;
```
*(Without `credentials: 'include'`, the browser will silently drop the `Set-Cookie` header on login, resulting in HTTP 401 on subsequent calls.)*

---

## 4. Required Headers & CSRF Protection

### 1. Universal Device Header: `X-Device-Id`
* **Mandatory on every single HTTP request** (both authenticated and unauthenticated).
* The frontend must generate a stable UUID (persisted in `localStorage.getItem('ztp_device_id')`) and include:
  ```http
  X-Device-Id: 7123c54a-5f2d-492f-9703-677be6d10dd7
  ```
* This header powers the Zero Trust device verification and anomaly detection engine.

### 2. CSRF Protection: `X-XSRF-TOKEN`
* **Mandatory on all mutating requests** (`POST`, `PUT`, `PATCH`, `DELETE`).
* **Bootstrap Endpoint**: `GET /api/csrf-token`
* **Cookie Name**: `XSRF-TOKEN`
* **Header Name**: `X-XSRF-TOKEN`

#### Workflow:
1. Before firing the first mutating request, the frontend calls `GET /api/csrf-token` with `credentials: 'include'`.
2. The server responds with `Set-Cookie: XSRF-TOKEN=<token>; SameSite=None; Secure; Path=/`.
3. The frontend reads `document.cookie` to extract the `XSRF-TOKEN` value.
4. The frontend passes that value in the HTTP header:
   ```http
   X-XSRF-TOKEN: <extracted-token-value>
   ```

---

## 5. End-to-End Authentication Flows

### Flow 1: Standard Password Login & Verification Gates
```
[Frontend] POST /api/auth/login
  Headers: X-Device-Id: <uuid>, X-XSRF-TOKEN: <token>
  Body: { "email": "adaeze@example.com", "password": "SuperSecret123" }
```
**Possible Responses**:
1. **Unrecognized Device Gate**:
   ```json
   {
     "success": true,
     "message": "Login successful",
     "data": {
       "user": { "id": 1, "username": "adaeze", "email": "adaeze@example.com", "role": "SUPER_ADMIN" },
       "deviceVerificationRequired": true,
       "twoFactorRequired": false
     }
   }
   ```
   * *Action*: Redirect user to `/auth/verify-device.html`. Backend sends a 6-digit OTP code to their email.
   * *Verification Request*:
     ```http
     POST /api/auth/verify-device
     Body: { "email": "adaeze@example.com", "code": "123456", "rememberDevice": true }
     ```
   * *Result*: Backend sets `SESSION` cookie, marks device trusted, and logs user in.

2. **Two-Factor Authentication Gate**:
   ```json
   {
     "success": true,
     "data": {
       "user": { ... },
       "deviceVerificationRequired": false,
       "twoFactorRequired": true
     }
   }
   ```
   * *Action*: Redirect user to `/auth/two-factor.html`.
   * *Verification Request*:
     ```http
     POST /api/auth/2fa/verify
     Body: { "email": "adaeze@example.com", "code": "123456" }
     ```
   * *Result*: Backend sets `SESSION` cookie and logs user in.

3. **Known Trusted Device (Direct Grant)**:
   ```json
   {
     "success": true,
     "data": {
       "user": { ... },
       "deviceVerificationRequired": false,
       "twoFactorRequired": false
     }
   }
   ```
   * *Action*: Immediate session granted; redirect to dashboard based on `user.role`.

### Flow 2: Emergency Recovery-Phrase Access
Used when a user has lost device or 2FA access:
```http
POST /api/auth/recover-with-phrase
Headers: X-Device-Id: <uuid>, X-XSRF-TOKEN: <token>
Body: {
  "email": "user@example.com",
  "phrase": "apple orange banana grape cherry lemon ...",
  "newPassword": "NewSecurePassword123!"
}
```
* Bypasses 2FA, resets the password hash, issues an audit trail record, and returns standard login verification status.

### Flow 3: Logout & Server-Side Invalidation
```http
POST /api/auth/logout
Headers: X-Device-Id: <uuid>, X-XSRF-TOKEN: <token>
```
* Invalidates the session server-side in `SPRING_SESSION` and clears the cookie.

---

## 6. High-Level Endpoint Groupings

All route definitions, query parameters, and payload structures are defined in the Spring Boot `@RestController` controllers (source of truth):

| Module | Base Path | Primary Controller | Key Responsibilities |
|---|---|---|---|
| **Auth & CSRF** | `/api/auth/**`, `/api/csrf-token` | [`AuthController.java`](src/main/java/com/ztp/auth/AuthController.java), [`CsrfController.java`](src/main/java/com/ztp/auth/CsrfController.java) | Login, device verification, 2FA, password recovery, session logout, `/api/auth/me`. |
| **Account Self-Service** | `/api/account/**` | [`AccountController.java`](src/main/java/com/ztp/account/AccountController.java), [`NotificationPreferenceController.java`](src/main/java/com/ztp/notification/NotificationPreferenceController.java) | Profile view/edit, avatar upload, password change, recovery phrase generation, notification preferences. |
| **Admin Users** | `/api/admin/users/**` | [`AdminUserController.java`](src/main/java/com/ztp/admin/AdminUserController.java) | Paginated user list (`page`, `pageSize`, `sort`, `dir`, `q`, `status`), provision user, suspend/unlock user. |
| **Admin Devices** | `/api/admin/devices/**`, `/api/devices/**` | [`AdminDeviceController.java`](src/main/java/com/ztp/admin/AdminDeviceController.java), [`DeviceController.java`](src/main/java/com/ztp/device/DeviceController.java) | Audit registered hardware, trust/untrust device, block suspect device IDs. |
| **Admin Sessions** | `/api/admin/sessions/**`, `/api/sessions/**` | [`AdminSessionController.java`](src/main/java/com/ztp/admin/AdminSessionController.java), [`SessionController.java`](src/main/java/com/ztp/session/SessionController.java) | View active concurrent sessions across the cluster, revoke individual or all active sessions. |
| **Admin Roles & RBAC** | `/api/admin/roles/**` | [`RoleController.java`](src/main/java/com/ztp/role/RoleController.java) | Read organization roles and permission matrices. |
| **Security Alerts & Threats** | `/api/admin/alerts/**`, `/api/admin/threats/**` | [`AlertController.java`](src/main/java/com/ztp/alert/AlertController.java), [`ThreatController.java`](src/main/java/com/ztp/threat/ThreatController.java) | Security event monitoring, threat resolution with labeled feedback (`CONFIRMED_THREAT` / `FALSE_POSITIVE`). |
| **Compliance & Reports** | `/api/admin/reports/**` | [`ReportController.java`](src/main/java/com/ztp/report/ReportController.java) | Export CSV and PDF compliance audit reports. |
| **In-App Notifications** | `/api/notifications/**` | [`NotificationController.java`](src/main/java/com/ztp/notification/NotificationController.java) | Real-time user/admin alerts, mark as read. |

---

## 7. CORS Configuration & Open Deployment Action Item

### Backend Allowed Origins
In [`SecurityConfig.java`](src/main/java/com/ztp/config/SecurityConfig.java), `corsConfigurationSource` permits:
* `http://localhost:5176` (Vite dev)
* `http://127.0.0.1:5176`
* `http://localhost:5173`
* `http://127.0.0.1:5173`
* `http://localhost:3000`
* `http://127.0.0.1:3000`
* Dynamic origin injected via `${APP_FRONTEND_URL}`

> [!WARNING]
> **OPEN ACTION ITEM: Production Frontend Domain**  
> Once the frontend is deployed to production (e.g., `https://ztp.vercel.app` or `https://ztp.netlify.app`), set the `APP_FRONTEND_URL` environment variable in the **Render Dashboard** $\rightarrow$ **Environment** to your exact production URL.  
> CORS with credentials (`allowCredentials = true`) **strictly forbids wildcards (`*`)**. The browser will block API calls if the origin does not match.
