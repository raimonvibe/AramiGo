# AramiGo at Troubigue

Beginner-friendly **Classical Syriac Aramaic** learning web app, served from
**[troubigue.com](https://troubigue.com)**.

The app is called **AramiGo** everywhere a learner sees it — nav, headings, install
name, share cards. Troubigue is where it lives, not what it is called; only the page
title and the deployment settings name the domain.

- **Frontend:** Next.js (App Router) + TypeScript — feature modules under `src/features/`
- **Backend:** Java Spring Boot 4 — hexagonal layout (`domain` / `application` / `infrastructure`)

UX references: `docs/references/`. Architecture notes: `docs/ARCHITECTURE.md`.  
Production (Render + Vercel): `docs/PRODUCTION.md`. Color / anti-Duolingo palette: `docs/COLOR.md`.  
Speech / listen mode: `docs/SPEECH.md` (Hebrew system voice as Syriac stand-in).

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

### How a unit is built

`section-1-unit-3.json` ("At the door") is the pattern to copy. A unit is **one
scene**, not a word list — an arrival at a house, learned line by line — because
a scene forces the recycling that actually makes vocabulary stick:

- **Seven new words per unit, no more.** Unit 3 adds only ܒܝܬܐ, ܥܘܠ, ܗܪܟܐ, ܗܘ,
  ܐܝܟܘ, ܬܪܥܐ, ܛܒܐ. Everything else in it is revision.
- **Every lesson reuses the last.** Lesson 2 needs lesson 1's words to make its
  sentences; the final `CHARACTER` lesson reassembles the whole scene.
- **Recognition before production.** A word is met in `TRANSLATE_TO_ENGLISH` or
  `LISTEN_CHOOSE_MEANING` before it is ever asked for in `TRANSLATE_TO_ARAMAIC`.
- **Distractors are words the learner already knows**, so a wrong tap is still
  revision rather than noise. Never introduce a word as a distractor.
- **Sentences stay at two or three words.**

### Grammar traps

The easy mistake is to build a Syriac sentence on an English sentence shape.
Two that bit unit 3 before it shipped:

- **"X is here" and "where is X?" need the copula.** Syriac supplies the enclitic
  pronoun where English uses "is": ܗܪܟܐ ܗܘ "he is here", ܐܒܝ ܗܪܟܐ ܗܘ "my father is
  here". For questions the pronoun contracts onto the interrogative — ܐܝܟܘ ܐܒܝ
  "where is my father?", the same form the Peshitta uses at Genesis 4:9
  (ܐܝܟܘ ܗܒܝܠ ܐܚܘܟ). Bare ܐܝܟܐ + a noun is not the attested pattern.
- **The present tense puts the pronoun after the participle**, not before:
  ܫܬܐ ܐܢܬ "you drink", the way the Peshitta writes ܡܨܠܐ ܐܢܬ "you pray" at
  Matthew 6:6. Fronting ܐܢܬ is grammatical but reads as emphatic.

Adjectives are fine the intuitive way round: noun first, adjective after, both in
the emphatic state — ܒܝܬܐ ܛܒܐ "a good house".

One more, from unit 4:

- **An imperative is its own form, not the verb you looked up.** Unit 4 is built
  on ܬܒ "sit", ܣܒ "take" and ܐܟܘܠ "eat" — the imperatives of ܝܬܒ, ܢܣܒ and ܐܟܠ,
  none of which look like their root. So a verb the learner already knows cannot
  simply be reused as a command: ܫܬܐ is "drinks", and "drink!" is ܐܫܬܝ. That is
  why unit 4 pours wine with ܣܒ ܚܡܪܐ "take wine" rather than asking anyone to
  drink it — the imperative would have been an eighth new word.

Format notes that bite:

- `correctTokens` separates alternate answers with `|` (`"a good house|good house"`).
  Don't mix a one-word and a multi-word alternative — the word bank then offers
  chips for both and the tip tells the learner to build a sentence.
- `MATCH_PAIRS` uses `script=meaning` pairs separated by `;`. The `;` is required
  once any meaning contains a space.
- Listening prompts are spoken by mapping the Syriac to Hebrew letters
  (`docs/SPEECH.md`), so every glyph used must exist in that map.

Lesson `position` is a single linear counter across the whole curriculum, so a
new unit's lessons continue the numbering rather than restarting at 1.

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
