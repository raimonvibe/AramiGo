# AramiGo — production deploy (Render + Vercel)

Stack stays as-is:

- **Backend:** Spring Boot (Java) on **Render** free Web Service  
- **Frontend:** Next.js on **Vercel**  
- Guest progress stays in the API’s in-memory H2 (resets when the free instance sleeps/restarts)

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

### 1.1 Create the service

1. [Render Dashboard](https://dashboard.render.com) → **New** → **Web Service**
2. Connect GitHub repo `raimonvibe/AramiGo`
3. Settings:

| Field | Value |
|--------|--------|
| Name | `aramigo-api` (or similar) |
| Region | closest to you |
| Root Directory | `backend` |
| Runtime | **Java** |
| Build Command | `./mvnw -DskipTests package` |
| Start Command | `java -jar target/aramigo-api-0.0.1-SNAPSHOT.jar` |
| Instance | **Free** |

> If Render’s Java runtime is awkward, use **Docker** instead (optional `backend/Dockerfile` later). Free tier still applies.

### 1.2 Environment variables (Render)

| Key | Value |
|-----|--------|
| `ARAMIGO_CORS_ALLOWED_ORIGINS` | Your Vercel URL, e.g. `https://aramigo.vercel.app` (no trailing slash). Add `http://localhost:3000` too if you still hit prod API from local UI: `https://aramigo.vercel.app,http://localhost:3000` |

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
| H2 in-memory | Progress/energy reset when the Render instance restarts or sleeps |
| No login | Progress is per guest key on the server, not a durable account |
| CORS mistakes | Symptom: browser blocks `/api/*` — fix origins + redeploy API |
| Wrong API URL | Symptom: frontend calls localhost in prod — set `NEXT_PUBLIC_API_URL` and redeploy |

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
