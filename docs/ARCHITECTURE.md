# AramiGo architecture

Grounded in **hexagonal architecture (ports & adapters)** for the Spring Boot API
and **feature modules** for the Next.js UI — the usual 2025–2026 guidance for
apps that expect to grow past a tutorial spike.

## Backend (`backend/`)

Dependencies point **inward only**:

```
infrastructure  →  application  →  domain
     (adapters)      (use cases)     (pure Java)
```

| Layer | Responsibility | Patterns |
|-------|----------------|----------|
| `domain/` | Models, enums, answer-matching policy, domain exceptions | Entity, Strategy, Value-ish records |
| `application/` | Use-case ports + orchestration | Ports & Adapters (inbound/outbound), Facade-style application service |
| `infrastructure/` | JPA, REST, CORS, curriculum seed | Adapter, Repository, Factory (curriculum), DTO mapping |

### Design patterns in play

- **Ports & Adapters** — controllers and JPA never reach into each other; both talk through application ports.
- **Strategy** — `AnswerMatchingPolicy` (token/`|` alternatives) is swappable without touching use cases.
- **Repository** — outbound ports hide persistence; Spring Data lives only in adapters.
- **Data-driven curriculum** — content is JSON under `resources/curriculum/`, upserted by `slug` so ids (and learner progress) survive content edits.
- **DTO at the edge** — HTTP records stay in `infrastructure/web`; domain models never leak JSON annotations.

Domain code must not import Spring, JPA, or servlet types.

## Frontend (`frontend/`)

```
app/          thin routes only
features/     learning-path, lesson, auth (UI + hooks)
shared/       api client, ui primitives, styles
```

### Identity

`X-Guest-Key` (a `localStorage` uuid) or a Google ID token in `Authorization`.
`LearnerIdentityResolver` turns headers into an opaque identity key — `guest:…` or
`google:…` — before the application service sees them, so the learning module never
knows what authentication is. Signing in merges the guest learner into the account.

### Anti-cheat

`completeLesson` checks recorded correct answers (`exercise_progress`) rather than
trusting the client, and only pays out the first time a lesson is finished.

Pages orchestrate; interactive lesson/path UI lives under `features/`.
