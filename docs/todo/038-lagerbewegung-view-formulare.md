---
Titel: LagerbewegungView + WareineingangFormular + WarenausgangFormular
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Implementiere die Seite für manuelle Lagerbewegungen mit zwei Formularen: Wareneingang und Warenausgang. Das Wareneingang-Formular prüft die Geschäftsregel, dass eine offene Bestellung existieren muss, und zeigt nach erfolgreicher Buchung einen Bestätigungsdialog.

Referenz: Masterplan Unit 10 — R-M1, R-M2, R-M3, R-M4

**Zu erstellende Dateien:**
- `frontend/src/views/LagerbewegungView.vue`
- `frontend/src/components/WareineingangFormular.vue`
- `frontend/src/components/WarenausgangFormular.vue`
- `frontend/src/api/services/bewegungService.js`

**WareineingangFormular.vue:**
- Artikel-Dropdown (aus `GET /api/v1/artikel`, nur AKTIVE)
- Menge (`InputNumber`, Pflichtfeld)
- Datum (`DatePicker`, Pflichtfeld)
- Submit: `POST /api/v1/bewegungen/eingang`
- Bei `422`-Fehler: klare Fehlermeldung "Keine offene Bestellung für diesen Artikel vorhanden"
- Nach Erfolg: PrimeVue `ConfirmDialog` "Soll die Bestellung als geliefert markiert werden?"
  - "Ja" → `POST /api/v1/bewegungen/eingang/{transaktionId}/bestellung-abschliessen`
  - "Nein" → Dialog schließen, Formular zurücksetzen

**WarenausgangFormular.vue:**
- Artikel-Dropdown (nur AKTIVE)
- Menge (`InputNumber`, Pflichtfeld)
- Buchungstyp-Dropdown: `Verbrauch intern`, `Verkauf`, `Verlust/Schwund`, `Retoure`
- Datum (`DatePicker`, Pflichtfeld)
- Freitext-Grund (Textarea, optional)
- Submit: `POST /api/v1/bewegungen/ausgang`
- Bei `422`-Fehler: "Nicht genügend Bestand vorhanden"

## Akzeptanzkriterien

- [x] Wareneingang-Formular zeigt `422`-Fehler klar als Fehlermeldung (nicht als Popup)
- [x] Nach erfolgreichen Wareneingang: `ConfirmDialog` mit Ja/Nein-Auswahl
- [x] "Ja" schließt die verknüpfte Bestellung (`POST /bestellung-abschliessen`)
- [x] Buchungstyp beim Ausgang ist ein Pflicht-Dropdown mit den 4 festen Optionen
- [x] Beide Formulare setzen sich nach erfolgreichem Submit zurück
- [x] Beide Formulare haben clientseitige Pflichtfeld-Validierung vor Submit
- [x] `<script setup>` Composition API durchgängig

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/lagerbewegungView.test.js` — 6/6 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/lagerbewegungen.md
