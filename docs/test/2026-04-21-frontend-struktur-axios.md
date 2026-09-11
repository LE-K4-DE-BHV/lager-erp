# Testergebnis: Vue-Projektstruktur (031) + Axios-Konfiguration (032)

**Datum**: 2026-04-21  
**Tickets**: `docs/todo/031-vue-projektstruktur-vite.md`, `docs/todo/032-axios-konfiguration-auth-service.md`  
**Tester**: kev-tester  
**Gesamt-Status**: ✅ GRÜN — Alle 9 Ticket-spezifischen Tests bestanden

---

## Getestete Komponenten

| Datei | Testmethode |
|---|---|
| `frontend/src/api/axios.js` | Vitest: `axios.test.js` (5 Tests) |
| `frontend/src/api/services/authService.js` | Vitest: `authService.test.js` (4 Tests) |
| `frontend/package.json` + Vite-Build | `npm ci` + `npm run build` (im Docker-Build) |
| `frontend/src/main.js` + `App.vue` | Vite-Kompilierung: 303 Module transformiert |

---

## Ticket 031 — Vue-Projektstruktur

**Testbefehl**: `docker compose build` (Stage: `node:20-alpine` → `npm ci` → `npm run build` → `nginx:alpine`)  
**Ergebnis**: ✅ BUILD SUCCESS — beide Images gebaut

| Prüfung | Ergebnis |
|---|---|
| `npm ci --quiet` (196 packages) | ✅ 0 vulnerabilities |
| `vite build` (303 Module) | ✅ built in 4.03s |
| `dist/index.html` vorhanden | ✅ 0.48 kB |
| `dist/assets/` vorhanden | ✅ (JS + CSS für alle Views) |
| `LoginView`, `DashboardView`, `ArtikelView`, `LieferantenView`, etc. | ✅ alle als separate Chunks gebaut |
| Frontend-Image (`lager_management-frontend`) | ✅ läuft auf Port 80, HTTP 200 |

---

## Ticket 032 — Axios-Konfiguration + AuthService

**Testbefehl**: `npx vitest run --reporter=verbose` (axios.test.js + authService.test.js)  
**Ergebnis**: 9/9 Tests ✅ GRÜN

### Coverage-Übersicht

| Datei | Tests | Ergebnis |
|---|---|---|
| `axios.test.js` | 5/5 | ✅ |
| `authService.test.js` | 4/4 | ✅ |

### Ausgeführte Tests: axios.test.js

| Testmethode | Beschreibung | Ergebnis |
|---|---|---|
| `nutzt baseURL /api/v1` | `apiClient.defaults.baseURL === '/api/v1'` | ✅ |
| `sendet Credentials (withCredentials: true)` | `apiClient.defaults.withCredentials === true` | ✅ |
| `hat einen Response-Interceptor registriert` | `interceptors.response.handlers.length > 0` | ✅ |
| `leitet bei 401 auf /login um` | `router.push('/login')` wird aufgerufen | ✅ |
| `leitet bei 500 NICHT auf /login um` | `router.push` wird NICHT aufgerufen | ✅ |

### Ausgeführte Tests: authService.test.js

| Testmethode | Beschreibung | Ergebnis |
|---|---|---|
| `login() sendet POST an /auth/login mit HTTP-Basic-Auth` | Korrekte Auth-Parameter `{ auth: { username, password } }` | ✅ |
| `logout() sendet POST an /auth/logout` | POST-Aufruf auf `/auth/logout` | ✅ |
| `me() sendet GET an /auth/me` | GET-Aufruf auf `/auth/me` | ✅ |
| `login() wirft bei fehlgeschlagener Auth weiter` | Promise wird rejected mit '401 Unauthorized' | ✅ |

---

## Hinweis: Fehler in anderen Testdateien (NICHT Ticket 031/032)

Der vollständige Vitest-Lauf (alle 5 Test-Dateien) ergab **3 fehlgeschlagene Tests** — diese gehören zu Tickets mit Status `[OPEN]` und sind **nicht Bestandteil des aktuellen Testumfangs**:

| Test | Ticket | Status Ticket | Ursache |
|---|---|---|---|
| `loginView.test.js > leitet nach erfolgreichem Login auf /dashboard weiter` | 034 | `[OPEN]` | `wrapper.setValue()` nicht auf PrimeVue `InputText` aufrufbar — erwartet natives `<input>` |
| `loginView.test.js > zeigt Fehlermeldung bei falschem Login` | 034 | `[OPEN]` | Gleiche Ursache wie oben |
| `dashboardView.test.js > zeigt die Vorschlagstabelle mit Daten` | 035 | `[OPEN]` | PrimeVue `DataTable` rendert in JSDOM keine Zeilen-Inhalte; `wrapper.text()` enthält keine Artikelbezeichnungen |

**Bewertung**: Diese Tests wurden zusammen mit der Implementierung der Views (Tickets 034/035) vom Dev geschrieben. Da beide Tickets noch `[OPEN]` sind, sind die Fehler als **zukünftige Baustellen** zu behandeln. Der Dev muss bei Ticket 034 `wrapper.find('input')` statt `wrapper.find('InputText')` verwenden, und bei Ticket 035 PrimeVue global in der Testumgebung registrieren oder den Test auf `data-testid` umstellen.

---

## Gesamt-Ergebnis

| Ticket | Tests | Status |
|---|---|---|
| 031 — Vue-Projektstruktur | Docker-Build ✅, npm ci ✅, vite build ✅ | ✅ GRÜN |
| 032 — Axios-Konfiguration | 9/9 Vitest ✅ | ✅ GRÜN |

---

## Nächste Schritte

- ✅ **Ticket 031** → Status auf `[REVIEW]` gesetzt.
- ✅ **Ticket 032** → Status auf `[REVIEW]` gesetzt.
- ⚠️ **Hinweis für Ticket 034 (Dev)**: `wrapper.setValue()` auf PrimeVue-Komponenten — Selector auf `input[data-pc-name]` oder das native `<input>` anpassen.
- ⚠️ **Hinweis für Ticket 035 (Dev)**: PrimeVue in Vitest-Setup global registrieren oder `data-testid` auf Wrapper-Elemente setzen, die kein JSDOM-Rendering benötigen.
