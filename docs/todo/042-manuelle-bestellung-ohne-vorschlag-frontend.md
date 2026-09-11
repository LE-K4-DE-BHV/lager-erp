---
Titel: Manuelle Bestellung ohne Vorschlag – Frontend-UI
Status: [OPEN]
Zuweisung: Frontend Dev
Priorität: Medium
---

## Beschreibung

Neue UI-Möglichkeit zum Aufgeben einer manuellen Bestellung ohne vorherigen Bestellvorschlag. Der Nutzer kann direkt aus der `BestellhistorieView` heraus (Button "Neue Bestellung") ein Modal öffnen, Artikel, Lieferant, Menge und optionale Felder eingeben und die Bestellung absenden.

Referenz: Ticket 041 (Backend-Endpunkt `POST /api/v1/bestellungen`), Masterplan Unit 10 — R-N4, R-O1, R-O3

**Voraussetzung:** Ticket 041 muss den Status [DONE] erreicht haben (Backend-Endpunkt muss existieren).

**Zu erstellende / zu ändernde Dateien:**

- Erstellen: `frontend/src/components/ManuelleBestell Modal.vue`  
  → Neues Modal-Formular für manuelle Bestellungen
- Modifizieren: `frontend/src/views/BestellhistorieView.vue`  
  → "Neue Bestellung"-Button oben rechts im Header der View
- Modifizieren: `frontend/src/api/services/bestellungService.js`  
  → neue Funktion `erstelleManuelleBestellung(payload)`

**Einstiegspunkt im UI:**

Der Button "Neue Bestellung" erscheint in der `BestellhistorieView` oben rechts (PrimeVue `Button` mit Icon `pi-plus`). Er öffnet das `ManuelleBestellModal`.

```
BestellhistorieView
┌─────────────────────────────────────────────────────────┐
│ Bestellhistorie                    [+ Neue Bestellung]  │
├─────────────────────────────────────────────────────────┤
│ Tabelle: alle Bestellungen (bestehend)                  │
└─────────────────────────────────────────────────────────┘
```

**`ManuelleBestellModal.vue` – Felder:**

| Feld | Komponente | Pflicht | Beschreibung |
|---|---|---|---|
| Artikel | PrimeVue `Select` (Dropdown) | ✅ | Lädt aktive Artikel via `GET /api/v1/artikel`, zeigt Artikelnummer + Bezeichnung |
| Lieferant | PrimeVue `Select` (Dropdown) | ✅ | Lädt alle Lieferanten via `GET /api/v1/lieferanten`, zeigt Name |
| Bestellmenge | PrimeVue `InputNumber` | ✅ | Ganzzahl, min=1 |
| Einkaufspreis | PrimeVue `InputNumber` | ❌ | Dezimalzahl, vorausgefüllt mit `artikel.einkaufspreis` wenn Artikel gewählt |
| Gewünschtes Lieferdatum | PrimeVue `DatePicker` | ❌ | Datum, nur Zukunft |
| Notiz | PrimeVue `Textarea` | ❌ | Freitext |

**Verhalten:**

- Beim Öffnen des Modals: Artikel- und Lieferantenliste werden geladen (falls noch nicht im Cache).
- Beim Wählen eines Artikels: `einkaufspreis`-Feld wird mit dem `einkaufspreis` des Artikels vorausgefüllt (überschreibbar).
- "Bestellung absenden"-Button ruft `POST /api/v1/bestellungen` auf.
- Bei Erfolg (`201 Created`):
  - Modal schließt sich.
  - PrimeVue `Toast` (Erfolgs-Meldung): "Bestellung `{bestellnummer}` wurde erfolgreich erstellt."
  - Bestellhistorie-Tabelle lädt neu, um die neue Bestellung anzuzeigen.
- Bei Fehler:
  - `400 Bad Request` → Inline-Fehler unter dem betroffenen Feld anzeigen.
  - `422 Business Exception` (z.B. inaktiver Artikel) → PrimeVue `Message` (Fehler-Banner) im Modal.
  - `404` → PrimeVue `Message` im Modal: "Artikel oder Lieferant nicht gefunden."

**`bestellungService.js` – neue Funktion:**

```javascript
export const erstelleManuelleBestellung = (payload) =>
  api.post('/bestellungen', payload);
```

## Akzeptanzkriterien

- [ ] Button "Neue Bestellung" ist in `BestellhistorieView` sichtbar und öffnet das Modal
- [ ] Artikel-Dropdown zeigt alle aktiven Artikel (Artikelnummer + Bezeichnung)
- [ ] Lieferanten-Dropdown zeigt alle Lieferanten (Name)
- [ ] Auswahl eines Artikels füllt den Einkaufspreis automatisch vor (überschreibbar)
- [ ] Bestellmenge akzeptiert nur positive Ganzzahlen (Validierung im Frontend)
- [ ] Pflichtfelder (Artikel, Lieferant, Bestellmenge) werden vor Absenden geprüft; fehlende Felder werden markiert
- [ ] Erfolgreiche Bestellung zeigt Toast mit Bestellnummer und aktualisiert die Tabelle
- [ ] Fehlerantwort `422` (inaktiver Artikel) zeigt klare Fehlermeldung im Modal
- [ ] Modal schließt sich bei "Abbrechen" ohne Aktion
- [ ] `<script setup>` Composition API durchgängig
- [ ] Bestehende Funktionalität von `BestellhistorieView` (Tabelle, Status-Badge) bleibt unverändert
---
