# Lager-Management-System

Interaktives Lager-Management-System für Kleinhändler und kleine/mittlere Unternehmen. Fullstack-Monorepo: Spring Boot 3.4.5 / Java 25 Backend, Vue 3 / PrimeVue 4 Frontend, PostgreSQL, Docker Compose.

## Projektstruktur

```
backend/    Spring Boot 3.4.5, Java 25, Maven — REST-API, Batch-Import, Reorder-Analyse
frontend/   Vue 3 (<script setup>), PrimeVue 4, Vite, Pinia, Axios
docs/
  plans/        Masterplan — verbindlicher Scope, einzige Quelle der Wahrheit für Features
  brainstorms/  Ursprung der Anforderungen (R-*)
  todo/         Tickets mit Status [OPEN] → [IN_PROGRESS] → [TESTING] → [REVIEW] → [DONE]
  dev/          Entwickler-Doku (API, Architektur, Setup, CI/CD)
  test/         Testberichte
  user/         Benutzerhandbücher
zdocs/      Zusätzliche Projektdefinitionen
.github/workflows/ci.yml   CI (GitHub Actions): Backend + Frontend, parallel, Docker-Build
```

## Tech-Stack

| Schicht | Technologie |
|---|---|
| Backend | Java 25, Spring Boot 3.4.5, Spring Data JPA, Spring Security (HTTP Basic + `JSESSIONID`) |
| DB | PostgreSQL, `schema.sql` (`ddl-auto=none`), **keine** Flyway/Liquibase |
| Frontend | Vue 3 Composition API, PrimeVue 4, Vite, Pinia, Axios (`/api/v1/*`) |
| Tests | JUnit 5 + Mockito + AssertJ + Testcontainers (Backend), Vitest + Vue Test Utils (Frontend) |
| CI/CD | GitHub Actions (`.github/workflows/ci.yml`), Deployment via `docker-compose` |

## Entwickler-Befehle

```bash
# Umgebungsvariablen
cp .env.example .env   # dann echte Werte eintragen, NIE .env committen

# Alles starten (DB + Backend + Frontend via Nginx)
docker compose up --build

# Backend lokal
cd backend && mvn verify        # Build + alle Tests (Testcontainers)

# Frontend lokal
cd frontend && npm ci && npm run test -- --run
```

## Ticket-Workflow

Der Masterplan (`docs/plans/`) wird von `kev-produkt-owner` in atomare Tickets unter `docs/todo/NNN-titel.md` zerlegt. Jedes Ticket durchläuft:

```
[OPEN] → [IN_PROGRESS] → [TESTING] → [REVIEW] → [DONE]
```

| Status | Zuständiger Agent |
|---|---|
| `[OPEN]` / `[IN_PROGRESS]` | `kev-backend-dev` oder `kev-frontend-dev` |
| `[TESTING]` | `kev-tester` |
| `[REVIEW]` | `/code-review` |
| `[DONE]` | `kev-techwriter` |

Grundregel für alle Agenten: **nichts erfinden, nichts erraten.** Ist eine Anforderung im Masterplan unklar oder ein API-Endpunkt/DTO nicht definiert, wird sofort gestoppt und der User informiert — nie eine Annahme getroffen.

## Subagenten (`.claude/agents/`)

- **kev-produkt-owner** — zerlegt Masterplan in Tickets, überwacht Status
- **kev-backend-dev** — Spring-Boot-Features nach Masterplan, Security-Checkliste, Unit-Tests
- **kev-frontend-dev** — Vue-Komponenten nach Masterplan, Vitest-Tests
- **kev-tester** — QA, JUnit/Vitest, Testberichte in `docs/test/`
- **kev-techwriter** — Doku in `docs/user/` (Anwender) und `docs/dev/` (API/Architektur)
- **kev-devops** — GitHub-Actions-Workflows, Docker, CI/CD-Debugging

Jeder Agent liest vor der Arbeit sein Ticket aus `docs/todo/` und den relevanten Masterplan-Abschnitt aus `docs/plans/`.

## Scope-Grenzen (bewusst nicht umgesetzt)

Multi-User/RBAC über einen Operator-User hinaus, Spring Batch (nur `@Scheduled`), Flyway/Liquibase, JWT/OAuth, HTTPS/TLS, WebSocket, automatische Bestellauslösung (nur Vorschläge, Human-in-the-Loop), CORS-Handling im Backend (übernimmt Nginx).

## Kommunikation & Konventionen

- **Dokumentation, Kommentare, Commit-Messages**: Deutsch
- **Code-Bezeichner** (Klassen, Methoden, Variablen): Englisch
- **Domain-Entitäten, DB-Tabellen, UI-Labels**: Deutsch (analog Masterplan, z. B. "Artikel", "Bestellvorschlag")
- **Backend**: Constructor-Injection (`@RequiredArgsConstructor`), DTOs als Java Records, kein String-SQL
- **Frontend**: ausschließlich `<script setup>`, keine Options API, keine hardcodierten API-Pfade
- Stil: pragmatisch, direkt — keine überflüssigen Erklärbär-Kommentare
