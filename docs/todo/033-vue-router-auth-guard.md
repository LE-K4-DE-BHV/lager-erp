---
Titel: Vue Router + Navigation Guard (Auth-Schutz)
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: High
---

## Beschreibung

Konfiguriere den Vue Router mit allen Anwendungsrouten und einem globalen Navigation Guard, der für geschützte Routen die Session-Gültigkeit via `GET /api/v1/auth/me` prüft. Nicht-authentifizierte Nutzer werden zu `/login` umgeleitet.

Referenz: Masterplan Unit 9 — R-A3, Abschnitt "router/index.js"

**Zu erstellende Datei:**
- `frontend/src/router/index.js`

**Routen:**

| Pfad | Komponente | Auth erforderlich |
|---|---|---|
| `/login` | `LoginView.vue` | Nein |
| `/dashboard` | `DashboardView.vue` | Ja |
| `/artikel` | `ArtikelView.vue` | Ja |
| `/lieferanten` | `LieferantenView.vue` | Ja |
| `/lagerbewegung` | `LagerbewegungView.vue` | Ja |
| `/transaktionen` | `TransaktionshistorieView.vue` | Ja |
| `/bestellhistorie` | `BestellhistorieView.vue` | Ja |
| `/` | Redirect zu `/dashboard` | — |

**Navigation Guard (beforeEach):**
```javascript
router.beforeEach(async (to) => {
  if (to.meta.requiresAuth) {
    try {
      await authService.me()
    } catch {
      return '/login'
    }
  }
})
```

**Routen-Metadaten:** `meta: { requiresAuth: true }` für alle geschützten Routen.

**Lazy Loading:** Views werden mit `() => import(...)` lazy-loaded für bessere Performance.

## Akzeptanzkriterien

- [x] Alle 7 Anwendungsrouten sind konfiguriert (inkl. `/login`)
- [x] Navigation Guard prüft Session via `GET /api/v1/auth/me`
- [x] Direkter Aufruf von `/dashboard` ohne Session → Weiterleitung zu `/login`
- [x] Nach Logout → 401-Response → Weiterleitung zu `/login`
- [x] Alle Views werden lazy-loaded (`() => import(...)`)
- [x] `meta: { requiresAuth: true }` ist auf allen geschützten Routen gesetzt

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/router.test.js` — 5/5 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-uebersicht.md
