# Testergebnis: Frontend Views & Infrastruktur (Tickets 033–040)

**Datum**: 2026-04-23  
**Tickets**: 
- `docs/todo/033-vue-router-auth-guard.md`
- `docs/todo/034-login-view-auth-store.md`
- `docs/todo/035-dashboard-view-vorschlaege.md`
- `docs/todo/036-artikel-view-formular.md`
- `docs/todo/037-lieferanten-view.md`
- `docs/todo/038-lagerbewegung-view-formulare.md`
- `docs/todo/039-historien-views.md`
- `docs/todo/040-frontend-dockerfile-nginx.md`  
**Status**: ✅ GRÜN

---

## Getestete Klassen / Komponenten

| Test-Datei | Tests | Views / Komponenten |
|------------|-------|---------------------|
| `router.test.js` | 5 | `router/index.js` — Navigation Guard |
| `loginView.test.js` | 4 | `LoginView.vue`, `authStore.js` |
| `dashboardView.test.js` | 6 | `DashboardView.vue`, `BestellvorschlagTabelle.vue`, `BestellModal.vue`, `KpiSummary.vue` |
| `artikelView.test.js` | 5 | `ArtikelView.vue`, `ArtikelFormular.vue`, `artikelService.js` |
| `lieferantenView.test.js` | 6 | `LieferantenView.vue`, `lieferantenService.js` |
| `lagerbewegungView.test.js` | 6 | `LagerbewegungView.vue`, `WareineingangFormular.vue`, `WarenausgangFormular.vue`, `bewegungService.js` |
| `historienViews.test.js` | 9 | `TransaktionshistorieView.vue`, `BestellhistorieView.vue`, `transaktionService.js` |
| `authService.test.js` | 4 | `authService.js` |
| `axios.test.js` | 5 | `axios.js` — Konfiguration |

**Gesamt Frontend-Tests:** 50 Tests, 0 Fehler, 0 Failures

---

## Test-Ausführung

```
npm run test -- --run

 RUN  v3.2.4

 ✓ src/__tests__/router.test.js          (5 Tests)   168ms
 ✓ src/__tests__/lieferantenView.test.js (6 Tests)   307ms
 ✓ src/__tests__/artikelView.test.js     (5 Tests)   384ms
 ✓ src/__tests__/historienViews.test.js  (9 Tests)  1057ms
 ✓ src/__tests__/lagerbewegungView.test.js (6 Tests) 993ms
 ✓ src/__tests__/dashboardView.test.js   (6 Tests)   342ms
 ✓ src/__tests__/loginView.test.js       (4 Tests)   202ms
 ✓ src/__tests__/authService.test.js     (4 Tests)    58ms
 ✓ src/__tests__/axios.test.js           (5 Tests)  4736ms

 Test Files  9 passed (9)
      Tests  50 passed (50)
```

---

## Akzeptanzkriterien-Prüfung

### Ticket 033 — Vue Router + Auth Guard

- [x] Alle 7 Routen konfiguriert (inkl. `/login` und `/` → redirect)
- [x] Navigation Guard leitet bei 401 auf `/login` um (per Test verifiziert)
- [x] Direkter Aufruf `/dashboard` ohne Session → `/login`
- [x] Alle Views lazy-loaded (`() => import(...)`)
- [x] `meta: { requiresAuth: true }` auf ≥ 6 geschützten Routen (per Test verifiziert)

### Ticket 034 — LoginView + AuthStore

- [x] Login-Formular mit PrimeVue-Komponenten
- [x] Erfolgreicher Login → Weiterleitung `/dashboard`
- [x] Fehlgeschlagener Login → Inline-Fehlermeldung
- [x] Kein Passwort im Store / LocalStorage gespeichert
- [x] `authStore` mit Pinia Composition API

### Ticket 035 — DashboardView + KPI + Vorschläge

- [x] KPI-Summary zeigt Werte aus `GET /api/v1/dashboard/kpis`
- [x] Quick-Link-Kacheln navigieren korrekt
- [x] Vorschlagstabelle mit Farbkodierung (Rot/Gelb/Grau)
- [x] Schnellbestellen / Ignorieren / Bearbeiten-Aktionen
- [x] Reorder-Trigger-Button mit Spinner

### Ticket 036 — ArtikelView + Formular

- [x] Artikeltabelle mit allen Spalten
- [x] `artikelnummer` im Bearbeitungs-Formular read-only
- [x] Doppelte `artikelnummer` → Inline-Fehlermeldung (kein Alert)
- [x] Pflichtfeld-Validierung clientseitig
- [x] Deaktivieren-Aktion mit ConfirmDialog

### Ticket 037 — LieferantenView

- [x] Lieferantentabelle mit allen Spalten
- [x] Kein "Neuen Lieferanten anlegen"-Button (Batch-only)
- [x] Dialog erlaubt nur `name`, `kontaktEmail`, `kontaktTelefon`, `leadTimeTage`
- [x] `lieferantId` read-only im Dialog
- [x] Info-Text zu Batch-only-Anlage sichtbar

### Ticket 038 — LagerbewegungView + Formulare

- [x] Wareneingang-Formular zeigt 422-Fehler als Fehlermeldung
- [x] Nach Wareneingang: ConfirmDialog mit Ja/Nein
- [x] "Ja" schließt die Bestellung
- [x] Buchungstyp beim Ausgang: Pflicht-Dropdown mit 4 festen Optionen
- [x] Beide Formulare setzen sich nach Submit zurück

### Ticket 039 — TransaktionshistorieView + BestellhistorieView

- [x] Transaktionshistorie lädt paginiert (50 Einträge/Seite)
- [x] Filter nach `artikelId`, `von`, `bis`
- [x] Typ-Spalte zeigt EINGANG/AUSGANG (per Test verifiziert)
- [x] Bestellhistorie zeigt alle Bestellungen
- [x] Status-Badge OFFEN/GELIEFERT sichtbar
- [x] Beide Views Read-Only

### Ticket 040 — Frontend Dockerfile + nginx.conf

- [x] Multi-Stage Dockerfile: `node:20-alpine` → `nginx:alpine`
- [x] `npm ci` für reproduzierbare Builds
- [x] `nginx.conf` leitet `/api/**` an `http://backend:8080` weiter
- [x] SPA-Fallback `try_files $uri $uri/ /index.html` konfiguriert
- [x] Statische Assets mit `Cache-Control: public, immutable` gecacht
- [x] Port 80 exponiert

---

## Gefundene Fehler

> Keine. Alle 50 Tests grün, alle Akzeptanzkriterien erfüllt.

---

## Nächste Schritte

- ✅ Tickets 033–040 auf `[REVIEW]` gesetzt.
