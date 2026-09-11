---
Titel: LieferantenView – Lieferantenliste & Bearbeitungs-Formular
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Implementiere die Lieferantenverwaltungs-Seite. Lieferanten können per UI NUR bearbeitet werden — das Anlegen neuer Lieferanten ist ausschließlich via Batch-Import möglich. Daher gibt es **keinen** "Neuen Lieferanten anlegen"-Button.

Referenz: Masterplan Unit 10 — R-V1, R-V2

**Zu erstellende Dateien:**
- `frontend/src/views/LieferantenView.vue`
- `frontend/src/api/services/lieferantService.js`

**LieferantenView.vue:**
- PrimeVue `DataTable` mit allen Lieferanten
- Spalten: Lieferant-ID, Name, E-Mail, Telefon, Lead Time (Tage)
- Aktion pro Zeile: [Bearbeiten] — öffnet Inline-Edit oder Dialog
- **Kein** "Neuen Lieferanten anlegen"-Button!

**Bearbeitungs-Formular (Dialog oder Inline):**
- Editierbare Felder: `name`, `kontaktEmail`, `kontaktTelefon`, `leadTimeTage`
- Nicht editierbar: `lieferantId` (read-only angezeigt, zur Orientierung)
- Buttons: "Speichern" / "Abbrechen"
- Aufruf: `PUT /api/v1/lieferanten/{id}`

**Hinweis für den Nutzer:** Ein sichtbarer Info-Text erklärt, dass neue Lieferanten nur per Import-Datei angelegt werden können.

## Akzeptanzkriterien

- [x] Lieferantentabelle zeigt alle Lieferanten mit korrekten Spalten
- [x] **Kein** Anlegen-Button vorhanden (Batch-only)
- [x] Bearbeitungs-Dialog erlaubt nur `name`, `kontaktEmail`, `kontaktTelefon`, `leadTimeTage`
- [x] `lieferantId` ist im Dialog als read-only sichtbar (nicht editierbar)
- [x] Info-Text erklärt Batch-only-Anlage neuer Lieferanten
- [x] Nach Bearbeitung: Tabelle aktualisiert sich automatisch
- [x] `<script setup>` Composition API durchgängig

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/lieferantenView.test.js` — 6/6 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/lieferanten-verwaltung.md
