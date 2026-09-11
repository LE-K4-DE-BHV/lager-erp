---
Titel: AuthController – Login, Logout & Me-Endpunkt
Status: [IN_PROGRESS]

## Implementierungsnotiz
Implementierung abgeschlossen. Login-Endpunkt durch Spring Security formLogin abgefangen (kein Controller-Method nötig). `AuthController` behandelt `GET /me` und `POST /logout`. `AuthControllerTest` mit `@WebMvcTest` + eigener `TestSecurityConfig` — 8 Tests decken Happy Path, Error Path, Me, Logout und formLogin-Assertions ab. Kein Passwort/Hash wird geloggt.

## Testergebnis (2026-04-23) — FEHLGESCHLAGEN ❌
Fehler dokumentiert in `docs/test/2026-04-23-auth-controller.md`.
- **FEHLER-001**: `shouldReturn200WithUsernameOnSuccessfulLogin` — kein `JSESSIONID`-Cookie in der MockMvc-Response.
- **FEHLER-002**: `shouldReturnUsernameOnMeWithAuthenticatedSession` — `GET /api/v1/auth/me` gibt 404 zurück (Controller-Handler wird nicht aufgelöst, `ResourceHttpRequestHandler` greift stattdessen).
- **FEHLER-003**: `shouldInvalidateSessionOnLogout` — `POST /api/v1/auth/logout` gibt 404 zurück (selbe Ursache wie FEHLER-002).
Root Cause: `@WebMvcTest` + `@Import(TestSecurityConfig)` + `SecurityMockMvcRequestPostProcessors.user()` — Controller-Mapping wird nicht aufgelöst. Empfehlung: `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` erwägen oder `@WebMvcTest`-Konflikt debuggen.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `AuthController` mit den drei Auth-Endpunkten. Dieser Controller ist der einzige öffentlich zugängliche Einstiegspunkt in die API — alle anderen Endpunkte sind durch Spring Security gesichert.

Referenz: Masterplan Unit 4 — R-A1, Endpunkte `/api/v1/auth/`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/controller/AuthController.java`
- `backend/src/test/java/com/lagermanagement/space/web/controller/AuthControllerTest.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login via Spring Security, gibt `{ "username": "operator" }` zurück |
| `POST` | `/api/v1/auth/logout` | Session invalidieren, Security-Kontext leeren |
| `GET` | `/api/v1/auth/me` | Gibt aktuellen Username aus Security-Kontext zurück |

**Login-Response (Erfolg):** `200 OK`, Body: `{ "username": "operator" }`
**Login-Response (Fehler):** `401 Unauthorized`, Body via `GlobalExceptionHandler`

**Test-Szenarien:**
- Happy path: `POST /api/v1/auth/login` mit korrekten Credentials → `200`, Cookie `JSESSIONID` im Response-Header
- Error path: Login mit falschem Passwort → `401` mit JSON-Body (nicht HTML)
- Happy path: `GET /api/v1/auth/me` nach Login → `"operator"`
- Happy path: `POST /api/v1/auth/logout` → Session ungültig, danach `GET /api/v1/auth/me` → `401`

## Akzeptanzkriterien

- [ ] `POST /api/v1/auth/login` gibt bei Erfolg `200` mit `{ "username": "operator" }` zurück
- [ ] `POST /api/v1/auth/login` gibt bei Fehler `401` mit JSON-Body zurück (nicht Spring-Standard-HTML)
- [ ] `POST /api/v1/auth/logout` invalidiert die Session korrekt
- [ ] `GET /api/v1/auth/me` gibt `{ "username": "operator" }` zurück (nur bei aktiver Session)
- [ ] `AuthControllerTest` mit `@WebMvcTest` deckt alle 3 Endpunkte und Fehler-Szenarien ab
- [ ] Kein Passwort, kein Hash wird in Log-Ausgaben geschrieben
---
