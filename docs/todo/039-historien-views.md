---
Titel: TransaktionshistorieView + BestellhistorieView
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Implementiere zwei Read-Only-Historien-Seiten: die Transaktionshistorie mit serverseitiger Paginierung und Filterung sowie die Bestellhistorie als chronologische Liste aller Bestellungen.

Referenz: Masterplan Unit 10 — R-T1, R-T2, R_2.4.5

**Zu erstellende Dateien:**
- `frontend/src/views/TransaktionshistorieView.vue`
- `frontend/src/views/BestellhistorieView.vue`
- `frontend/src/api/services/transaktionService.js`

**TransaktionshistorieView.vue:**
- PrimeVue `DataTable` mit serverseitiger Paginierung
- Filter-Bereich oben:
  - Artikel-Dropdown (optional, `GET /api/v1/artikel`)
  - Datum-von / Datum-bis (PrimeVue `DatePicker`)
  - [Filter anwenden]-Button
- Tabellen-Spalten: Datum, Artikelnummer, Bezeichnung, Typ (EINGANG/AUSGANG), Menge, Buchungstyp, Quelle (BATCH/MANUELL), Benutzer
- API: `GET /api/v1/transaktionen?artikelId=...&von=...&bis=...&seite=...&groesse=50`

**BestellhistorieView.vue:**
- PrimeVue `DataTable` ohne Paginierung (alle Bestellungen)
- Tabellen-Spalten: Bestellnummer, Datum, Artikel, Lieferant, Menge, Status, Erstellt von
- Status-Badge: `OFFEN` (blau) / `GELIEFERT` (grün)
- API: `GET /api/v1/bestellungen`
- Detailansicht-Link pro Zeile (optional: Dialog oder neue Seite)

## Akzeptanzkriterien

- [x] Transaktionshistorie lädt paginiert (50 Einträge/Seite)
- [x] Filter nach `artikelId`, `von`, `bis` funktioniert korrekt
- [x] Typ-Spalte zeigt `EINGANG`/`AUSGANG` klar (unterschiedliche Farbe/Icon)
- [x] Bestellhistorie zeigt alle Bestellungen absteigend nach Datum
- [x] Status-Badge unterscheidet `OFFEN` (blau) und `GELIEFERT` (grün) visuell
- [x] Beide Views sind Read-Only (keine Aktions-Buttons für Änderungen)
- [x] `<script setup>` Composition API durchgängig

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/historienViews.test.js` — 9/9 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/historien.md
