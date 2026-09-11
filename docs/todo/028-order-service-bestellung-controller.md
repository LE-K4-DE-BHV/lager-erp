---
Titel: OrderService + BestellungController – Bestellvorschlag-Lifecycle
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `OrderService` für die Bestellfreigabe (Schnellbestellung + Bearbeitet-Bestellung) und den Ignorieren-Mechanismus. Der Service generiert die Bestellnummer, setzt Vorschlag-Status, erstellt PDF + XML und speichert den Audit Trail.

Referenz: Masterplan Unit 8 — R-L1, R-L2, R-L3, R-L5, R-O1, R-O2, R-O3

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/OrderService.java`
- `backend/src/main/java/com/lagermanagement/space/web/controller/BestellungController.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/BestellungCreateRequest.java` (Record)
- `backend/src/main/java/com/lagermanagement/space/web/dto/BestellungDto.java` (Record)
- `backend/src/test/java/com/lagermanagement/space/service/OrderServiceTest.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/v1/bestellungen/schnell/{vorschlagId}` | Schnellbestellung mit Standardwerten |
| `POST` | `/api/v1/bestellungen/bearbeitet/{vorschlagId}` | Bestellung mit angepassten Werten |
| `PUT` | `/api/v1/bestellvorschlaege/{vorschlagId}/ignorieren` | Vorschlag auf IGNORIERT setzen |
| `GET` | `/api/v1/bestellungen` | Alle Bestellungen (absteigend) |
| `GET` | `/api/v1/bestellungen/{id}` | Einzelne Bestellung |

**OrderService.erstelleBestellung()-Ablauf:**
1. Generiere Bestellnummer: `BEST-{YYYY}-{NNN}` (sequenziell pro Jahr)
2. Vorschlag-Status → `BESTELLT`, `bestellung_id` setzen
3. Bestellung speichern (`erstelltVon = SecurityUtils.getCurrentUsername()`)
4. `PdfGeneratorService.generate(bestellung)` → PDF in `Output/Orders/`
5. `XmlGeneratorService.generate(bestellung)` → XML in `Output/Orders/`
6. Rückgabe: `{ bestellungId, bestellnummer, pdfPfad, xmlPfad }`

**Bestellnummer-Format:** `BEST-2026-001`, `BEST-2026-002`, ... (pro Kalenderjahr zurücksetzen)

## Akzeptanzkriterien

- [ ] Schnellbestellung erstellt Bestellung mit `standardBestellmenge` des Artikels
- [ ] Bearbeitete Bestellung übernimmt angepasste Werte aus dem Request
- [ ] Vorschlag-Status wechselt zu `BESTELLT`, `bestellung_id` wird verknüpft
- [ ] `erstelltVon` in Bestellung wird via `SecurityUtils.getCurrentUsername()` gesetzt (Audit Trail)
- [ ] PDF und XML werden in `Output/Orders/` gespeichert
- [ ] `PUT /api/v1/bestellvorschlaege/{id}/ignorieren` setzt Status auf `IGNORIERT`
- [ ] `OrderServiceTest` deckt Schnellbestellung, Ignorieren, Audit-Trail ab
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-bestellung.md
