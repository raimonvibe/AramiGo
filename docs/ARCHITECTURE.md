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
- **Factory** — `BeginnerSyriacCurriculumFactory` builds Unit 1 content in one place.
- **DTO at the edge** — HTTP records stay in `infrastructure/web`; domain models never leak JSON annotations.

Domain code must not import Spring, JPA, or servlet types.

## Frontend (`frontend/`)

```
app/          thin routes only
features/     learning-path, lesson (UI + hooks)
shared/       api client, ui primitives, styles
```

Pages orchestrate; interactive lesson/path UI lives under `features/`.
