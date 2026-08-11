# AramiGo — production deploy (Render + Vercel)

Stack stays as-is:

- **Backend:** Spring Boot (Java) on **Render** free Web Service
- **Frontend:** Next.js on **Vercel**
- **Database:** external Postgres (see [Choosing a database](#choosing-a-database)) — learner progress survives restarts

> Without `DATABASE_URL` the API falls back to in-memory H2, which is fine locally
> but loses every learner's progress on each restart. Set it in production.

Repo: https://github.com/raimonvibe/AramiGo

---

## 0. Local first (sanity check)

Terminal 1 — API:

```bash
cd backend
./mvnw spring-boot:run
```

Check: http://localhost:8080/health → `{"status":"healthy",...}`  
Path: http://localhost:8080/api/path

Terminal 2 — frontend:

```bash
cd frontend
npm install
npm run dev
```

Open: http://localhost:3000  
Lessons should load against `http://localhost:8080` by default.

---

## 1. Deploy the Java API on Render

Render has **no native Java runtime** (only Node, Python, Go, Rust, Ruby, Elixir, Docker).  
Use **Docker** — `backend/Dockerfile` builds the Spring Boot JAR inside the image.

### 1.1 Create the service

1. [Render Dashboard](https://dashboard.render.com) → **New** → **Web Service**
2. Connect GitHub repo `raimonvibe/AramiGo`
3. Settings:

| Field | Value |
|--------|--------|
| Name | `aramigo-api` (or similar) |
| Region | closest to you (e.g. Frankfurt) |
| Root Directory | `backend` |
| Language / Runtime | **Docker** |
| Dockerfile Path | `Dockerfile` (default; lives in `backend/`) |
| Build / Start Command | leave empty (Docker `ENTRYPOINT` runs the jar) |
| Instance | **Free** |

Render builds from the Dockerfile on each deploy. First build can take several minutes (Maven download).

### 1.2 Environment variables (Render)

| Key | Value |
|-----|--------|
| `ARAMIGO_CORS_ALLOWED_ORIGINS` | Your Vercel URL, e.g. `https://aramigo.vercel.app` (no trailing slash). Add `http://localhost:3000` too if you still hit prod API from local UI: `https://aramigo.vercel.app,http://localhost:3000` |
| `DATABASE_URL` | `jdbc:postgresql://HOST/DB?sslmode=require` — **required**, or progress is lost on every restart |
| `DATABASE_USERNAME` | Database user |
| `DATABASE_PASSWORD` | Database password |
| `GOOGLE_CLIENT_ID` | OAuth client id, or leave unset to run guests-only |

See `backend/.env.example` for the full list.

Render sets `PORT` automatically; `application.properties` already reads `${PORT:8080}`.

### 1.3 Health check

In Render service settings:

- **Health Check Path:** `/health`

Deploy, then open:

`https://YOUR-SERVICE.onrender.com/health`

### 1.4 Keepalive (free tier sleeps ~15 min)

1. GitHub repo → **Settings → Secrets and variables → Actions → Variables**
2. New variable:
   - Name: `RENDER_APP_URL`
   - Value: `https://YOUR-SERVICE.onrender.com` (no trailing slash)
3. Workflow already in repo: `.github/workflows/keepalive.yml` (pings `/health` every 10 minutes)

Run **Actions → Keep Render webapp awake → Run workflow** once to verify.

---

## 2. Deploy the Next.js frontend on Vercel

### 2.1 Import project

1. [Vercel](https://vercel.com) → **Add New… → Project**
2. Import `raimonvibe/AramiGo`
3. Configure:

| Field | Value |
|--------|--------|
| Framework Preset | Next.js |
| Root Directory | `frontend` |
| Build Command | `npm run build` (default) |
| Output | Next default |

### 2.2 Environment variables (Vercel)

| Key | Value | Environments |
|-----|--------|----------------|
| `NEXT_PUBLIC_API_URL` | `https://YOUR-SERVICE.onrender.com` | Production (and Preview if you want) |
| `NEXT_PUBLIC_GOOGLE_CLIENT_ID` | The same OAuth client id set on Render | Production (omit to run guests-only) |

**Important:** `NEXT_PUBLIC_*` is baked in at **build** time. After changing it, **redeploy** the frontend.

### 2.3 CORS must match

Whatever URL Vercel gives you (and custom domains) must appear in Render’s `ARAMIGO_CORS_ALLOWED_ORIGINS`. If you add a custom domain later, update Render and redeploy the API.

### 2.4 Deploy

Click **Deploy**. Open the Vercel URL → home path should load lessons from Render.

---

## 3. Smoke checklist (production)

1. `GET https://YOUR-SERVICE.onrender.com/health` → 200  
2. `GET https://YOUR-SERVICE.onrender.com/api/path` → JSON with nodes  
3. Vercel site loads; unit/chapters visible  
4. Start a lesson; CHECK an answer  
5. Browser Network tab: API calls go to Render, not `localhost`  
6. GitHub keepalive workflow succeeds (manual run)

---

## 4. Free-tier caveats (honest)

| Topic | Reality |
|--------|---------|
| Cold start | First request after sleep can take 30–60s; keepalive reduces this |
| Cold database | Neon suspends idle compute; the first query after that pays a wake-up too |
| No `DATABASE_URL` | Falls back to in-memory H2 and every learner loses progress on restart |
| Guest keys | Without signing in, progress lives in one browser's `localStorage` |
| CORS mistakes | Symptom: browser blocks `/api/*` — fix origins + redeploy API |
| Wrong API URL | Symptom: frontend calls localhost in prod — set `NEXT_PUBLIC_API_URL` and redeploy |

---

## 3a. This deployment's values

| Where | Key | Value |
|-------|-----|-------|
| Render | `DATABASE_URL` | The Neon connection string, pasted as-is (libpq form is converted at startup) |
| Render | `DATABASE_USERNAME` | `neondb_owner` |
| Render | `DATABASE_PASSWORD` | From the Neon dashboard |
| Render | `ARAMIGO_CORS_ALLOWED_ORIGINS` | `https://arami-go-gv7b.vercel.app` |
| Render | `GOOGLE_CLIENT_ID` | The OAuth client id |
| Vercel | `NEXT_PUBLIC_API_URL` | `https://aramigo.onrender.com` |
| Vercel | `NEXT_PUBLIC_GOOGLE_CLIENT_ID` | The same OAuth client id |

**No trailing slashes** in `ARAMIGO_CORS_ALLOWED_ORIGINS` or `NEXT_PUBLIC_API_URL`.
A browser's `Origin` header never carries one, so `https://arami-go-gv7b.vercel.app/`
will not match and every API call gets blocked by CORS.

---

## 4a. Choosing a database

Render's free Postgres is **not** a long-term option and no amount of pinging changes that:

| Option | Free-tier reality |
|--------|-------------------|
| **Neon** (recommended) | No expiry. Compute auto-suspends when idle and wakes on the next connection; hitting limits suspends compute but never deletes data. ~0.5 GB storage. |
| **Supabase** | ~500 MB, but a project **pauses after 7 days of inactivity** and needs a manual unpause. Here a scheduled ping *does* help. |
| **Render Postgres (free)** | Expires **30 days after creation** on a wall clock, regardless of traffic, then is deleted after a 14-day grace period. 1 GB, no backups, one per workspace. |
| **Render Postgres (paid)** | ~$6/month if you want everything on one platform. |

The two free timers are different things: the **web service** sleeps after 15 minutes idle
(which `keepalive.yml` fixes), while free **Render Postgres** counts down from creation
(which nothing fixes).

---

## 4b. Google sign-in setup

Sign-in is optional. Skip this whole section and the app runs guests-only.

1. [Google Cloud console](https://console.cloud.google.com/apis/credentials) → **Create credentials** → **OAuth client ID**
2. Application type: **Web application**
3. **Authorised JavaScript origins** — add every origin that will show the button:
   - `http://localhost:3000`
   - `https://arami-go-gv7b.vercel.app`
4. **Authorised redirect URIs** — leave empty. This uses Google Identity Services,
   which hands an ID token to the page; there is no redirect leg.
5. The Render URL is **not** entered anywhere in the OAuth client. The browser talks
   to Google directly, and the API only verifies the resulting token offline against
   Google's published keys.
6. Configure the **OAuth consent screen** (External). While its status is *Testing*,
   only accounts listed under **Test users** can sign in — add your own Google account
   there, or publish the app.
7. Vercel **preview** deployments get their own hostnames, which will not be in the
   origins list, so sign-in only works on production and localhost.
8. Copy the **client ID** into both:
   - Render → `GOOGLE_CLIENT_ID`
   - Vercel → `NEXT_PUBLIC_GOOGLE_CLIENT_ID`
9. Redeploy the frontend (`NEXT_PUBLIC_*` is baked in at build time).

You do **not** need to enable any API under *APIs & Services → Enabled APIs*.
Sign-in is a token exchange with Google's identity endpoints, not a Google Cloud
API call, so the enabled-API list is irrelevant here.

**The client secret is never used.** The browser gets a Google-signed ID token; the API
verifies its signature against Google's public keys and checks the issuer, audience and
expiry. A token minted for a different app is rejected.

When someone signs in, `POST /api/auth/link` folds their guest progress into the account —
best-of on lessons, sum on gems — so trying the app before making an account costs nothing.

---

## 5. Handy commands

```bash
# Local API
cd backend && ./mvnw spring-boot:run

# Local frontend (points at local API by default)
cd frontend && npm run dev

# Point local frontend at production API (optional)
cd frontend
NEXT_PUBLIC_API_URL=https://YOUR-SERVICE.onrender.com npm run dev
```

---

## 6. Order of operations (recommended)

1. Deploy **Render API** first → confirm `/health`  
2. Set GitHub `RENDER_APP_URL` → run keepalive once  
3. Deploy **Vercel** with `NEXT_PUBLIC_API_URL`  
4. Set Render `ARAMIGO_CORS_ALLOWED_ORIGINS` to the Vercel URL → **redeploy API**  
5. Smoke-test the live site  
