# AramiGo

Beginner-friendly **Classical Syriac Aramaic** learning web app.

- **Frontend:** Next.js (App Router) + TypeScript — feature modules under `src/features/`
- **Backend:** Java Spring Boot 4 — hexagonal layout (`domain` / `application` / `infrastructure`)

UX references: `docs/references/`. Architecture notes: `docs/ARCHITECTURE.md`.  
Production (Render + Vercel): `docs/PRODUCTION.md`. Color / anti-Duolingo palette: `docs/COLOR.md`.

## Quick start

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

API: http://localhost:8080

### Frontend

```bash
cd frontend
npm run dev
```

App: http://localhost:3000

## Architecture (short)

**Backend** — ports & adapters:

- `domain/` — models, answer-matching Strategy, domain exceptions (no Spring)
- `application/` — use-case ports + `LearningApplicationService`
- `infrastructure/` — JPA adapters, REST controller, CORS, curriculum Factory/seed

**Frontend** — thin routes, fat features:

- `app/` — routing only
- `features/learning-path`, `features/lesson` — UI + flow
- `shared/` — API client + shared UI

## Patterns applied

| Pattern | Where |
|---------|--------|
| Ports & Adapters | Backend package layout |
| Strategy | `AnswerMatchingPolicy` |
| Repository | `*RepositoryPort` + JPA adapters |
| Data-driven seed | `curriculum/*.json` + `CurriculumDataSeeder` (slug upsert) |
| Decorator | `TransactionalLearningFacade` |
| DTO at the edge | `infrastructure/web/dto` |

## API

Every request carries an identity: `X-Guest-Key` for anonymous learners, or
`Authorization: Bearer <Google ID token>` once signed in.

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | Liveness (Render + keepalive) |
| GET | `/api/path` | All units + learner stats |
| GET | `/api/lessons/{id}` | Lesson session |
| POST | `/api/exercises/check` | `{ exerciseId, tokens }` |
| POST | `/api/lessons/complete` | `{ lessonId }` — requires every exercise solved |
| GET | `/api/me` | Profile + stats |
| POST | `/api/auth/link` | Merge guest progress into a signed-in account |

Errors are always `{ status, code, message }`; the `message` is written for a learner.

## Adding lessons

Content lives in `backend/src/main/resources/curriculum/*.json` — not in Java.
Add or edit a file and restart; the seeder upserts by `slug`, so existing ids and
learner progress survive content edits, and anything removed from the files is pruned.

## Configuration

Copy `backend/.env.example` and `frontend/.env.example`. The two that matter:

- `DATABASE_URL` — without it the API uses in-memory H2 and loses progress on restart
- `GOOGLE_CLIENT_ID` / `NEXT_PUBLIC_GOOGLE_CLIENT_ID` — unset means guests-only, which works fine

## Keep Render awake (free tier)

Free Render web services sleep after ~15 minutes idle. (This does **not** apply to
free Render Postgres, which expires 30 days after creation no matter how often you
ping it — see `docs/PRODUCTION.md` for database options.)


`.github/workflows/keepalive.yml` pings `/health` every 10 minutes via GitHub Actions.

1. Push this project to GitHub (Actions enabled).
2. Deploy the Spring API on Render.
3. Repo **Settings → Secrets and variables → Actions → Variables**  
   - Name: `RENDER_APP_URL`  
   - Value: `https://your-service.onrender.com` (no trailing slash)
4. Optionally run **Actions → Keep Render webapp awake → Run workflow** once to verify.
