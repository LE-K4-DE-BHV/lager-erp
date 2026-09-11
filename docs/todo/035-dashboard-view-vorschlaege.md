---
Titel: DashboardView + KpiSummary + BestellvorschlagTabelle + BestellModal
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Implementiere das zentrale Dashboard mit KPI-Summary, Quick-Link-Kacheln und der interaktiven Bestellvorschlags-Tabelle mit allen drei Aktionen (Schnellbestellen, Bearbeiten, Ignorieren). Das Dashboard ist die Startseite der Anwendung.

Referenz: Masterplan Unit 10 — R-P1 bis R-P5, R-L1, R-L2, R-L3

**Zu erstellende Dateien:**
- `frontend/src/views/DashboardView.vue`
- `frontend/src/components/KpiSummary.vue`
- `frontend/src/components/BestellvorschlagTabelle.vue`
- `frontend/src/components/BestellModal.vue`
- `frontend/src/api/services/dashboardService.js`

**Dashboard-Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  KPI-Summary                                             │
│  [ X offene Vorschläge ]  [ Y kritisch ]  [ Letzter Lauf]│
│  [ Button: "Analyse jetzt ausführen" ]                   │
├─────────────────────────────────────────────────────────┤
│  Quick-Link-Kacheln (PrimeVue Card):                     │
│  [Lagerbewegung] [Artikel] [Lieferanten]                 │
│  [Transaktionen] [Bestellhistorie]                       │
├─────────────────────────────────────────────────────────┤
│  Bestellvorschläge-Tabelle (PrimeVue DataTable)          │
│  Farb-Logik:                                             │
│    Rot: VORSCHLAG + bestand <= sicherheitsbestand        │
│    Gelb: VORSCHLAG + bestand > sicherheitsbestand        │
│    Grau: BESTELLT (= "In Lieferung")                     │
│  Aktionen: [Schnellbestellen] [Bearbeiten] [Ignorieren]  │
└─────────────────────────────────────────────────────────┘
```

**BestellModal.vue — Felder:**
- Bestellmenge (editierbar, vorausgefüllt mit `vorgeschlageneMenge`)
- Lieferant (PrimeVue Dropdown aus Lieferantenliste)
- Gewünschtes Lieferdatum (PrimeVue DatePicker)
- Notiz (Textarea)
- Buttons: "Bestellung freigeben" / "Abbrechen"

**Reorder-Trigger-Button:** Lade-Spinner während `POST /api/v1/reorder/trigger`, danach Tabelle neu laden.

**"Ignorieren"-Aktion:** PrimeVue `ConfirmDialog` → bei Bestätigung `PUT /api/v1/bestellvorschlaege/{id}/ignorieren` → Vorschlag verschwindet aus Tabelle.

## Akzeptanzkriterien

- [x] KPI-Summary zeigt korrekte Werte aus `GET /api/v1/dashboard/kpis`
- [x] Quick-Link-Kacheln navigieren zu den richtigen Routen
- [x] Vorschlagstabelle mit korrekter Farbkodierung (Rot/Gelb/Grau)
- [x] Schnellbestellen: direkter API-Aufruf, Tabelle aktualisiert sich danach
- [x] Bearbeiten: `BestellModal` öffnet sich mit vorausgefüllten Werten
- [x] Ignorieren: Bestätigungsdialog erscheint vor Aktion
- [x] Reorder-Trigger-Button zeigt Spinner während der Anfrage
- [x] `<script setup>` Composition API wird durchgängig verwendet (kein Options API)

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/dashboardView.test.js` — 6/6 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/dashboard.md
