---
Titel: ArtikelView + ArtikelFormular (Anlage & Bearbeitung)
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Implementiere die Artikelverwaltungs-Seite mit Tabelle aller Artikel und dem Artikel-Formular für Anlage und Bearbeitung. Doppelte Artikelnummern werden inline gemeldet.

Referenz: Masterplan Unit 10 — R-N1, R-N2, R-N3, R-N4

**Zu erstellende Dateien:**
- `frontend/src/views/ArtikelView.vue`
- `frontend/src/components/ArtikelFormular.vue`
- `frontend/src/api/services/artikelService.js`

**ArtikelView.vue:**
- PrimeVue `DataTable` mit allen Artikeln (AKTIV + INAKTIV)
- Spalten: Artikelnummer, Bezeichnung, Einheit, Warengruppe, Lieferant, Bestand, Bestellpunkt, Status
- Aktionen pro Zeile: [Bearbeiten] [Deaktivieren]
- Button: "Neuen Artikel anlegen" öffnet `ArtikelFormular`

**ArtikelFormular.vue — Felder (R-N1):**
- `artikelnummer` — `InputText` (bei Bearbeitung: **read-only**)
- `bezeichnung` — `InputText`, Pflichtfeld
- `mengeneinheit` — `InputText`, Pflichtfeld
- `warengruppe` — `InputText`, Pflichtfeld
- `lieferant` — `Dropdown` mit Lieferantenliste, Pflichtfeld
- `sicherheitsbestand` — `InputNumber`
- `bestellpunkt` — `InputNumber`
- `standardBestellmenge` — `InputNumber`
- `einkaufspreis` — `InputNumber` (Dezimal)
- Buttons: "Speichern" / "Abbrechen"

**Fehlerbehandlung:**
- Doppelte `artikelnummer` (409 vom Backend) → Inline-Fehlermeldung "Artikelnummer bereits vorhanden"
- Pflichtfeld-Validierung clientseitig vor dem Submit

**Deaktivieren-Aktion:** PrimeVue `ConfirmDialog` → `PUT /api/v1/artikel/{id}/deaktivieren`

## Akzeptanzkriterien

- [x] Artikeltabelle zeigt alle Artikel (aktiv + inaktiv) mit korrekten Spalten
- [x] `artikelnummer` im Bearbeitungs-Formular ist read-only
- [x] Doppelte `artikelnummer` zeigt Inline-Fehlermeldung (kein Popup/Alert)
- [x] Pflichtfeld-Validierung läuft clientseitig vor Submit
- [x] Deaktivieren-Aktion erfordert Bestätigung (ConfirmDialog)
- [x] Nach Anlage/Bearbeitung: Tabelle aktualisiert sich automatisch
- [x] `<script setup>` Composition API durchgängig

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/artikelView.test.js` — 5/5 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/artikel-verwaltung.md
