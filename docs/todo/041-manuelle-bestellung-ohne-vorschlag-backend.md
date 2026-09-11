---
Titel: Manuelle Bestellung ohne Vorschlag – Backend-Endpunkt
Status: [OPEN]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Neuer REST-Endpunkt `POST /api/v1/bestellungen`, der eine Bestellung für einen beliebigen aktiven Artikel anlegt – **ohne** dass ein Bestellvorschlag vom Reorder-Analyse-Job existieren muss. Der bestehende Bestellvorschlag-Lifecycle (Schnellbestellen / Bearbeitet) sowie die zugehörigen Endpunkte bleiben **unverändert**.

Referenz: Masterplan Datenbankschema (`bestellungen`-Tabelle), Unit 8 — OrderService, PdfGeneratorService, XmlGeneratorService, R-O1, R-O2, R-O3

**Warum kein Schema-Update nötig:**  
Die `bestellungen`-Tabelle hat bereits kein Pflichtfeld `vorschlag_id`. Der Bezug wird über das nullable `bestellung_id` in `bestellvorschlaege` hergestellt – umgekehrt. Eine Bestellung ohne verknüpften Vorschlag ist datenbankschema-konform.

**Zu erstellende / zu ändernde Dateien:**

- Modifizieren: `backend/src/main/java/com/lagermanagement/space/service/OrderService.java`  
  → neue Methode `erstelleManuelleBestellung(ManuelleBestellungRequest request)`
- Modifizieren: `backend/src/main/java/com/lagermanagement/space/web/controller/BestellungController.java`  
  → neuer Endpunkt `POST /api/v1/bestellungen`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/dto/ManuelleBestellungRequest.java` (Record)
- Modifizieren: `backend/src/test/java/com/lagermanagement/space/service/OrderServiceTest.java`  
  → neue Testmethoden für manuelle Bestellung

**Neuer Endpunkt:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/v1/bestellungen` | Legt eine manuelle Bestellung ohne Vorschlags-Bezug an |

**Request-Body (`ManuelleBestellungRequest`):**

```json
{
  "artikelId": 1,
  "lieferantId": 2,
  "bestellmenge": 500,
  "einkaufspreis": 0.05,
  "gewuenschtesLieferdatum": "2026-05-15",
  "notiz": "Dringend – Bestand kritisch"
}
```

| Feld | Typ | Pflicht | Beschreibung |
|---|---|---|---|
| `artikelId` | Long | ✅ | Muss ein AKTIVER Artikel sein |
| `lieferantId` | Long | ✅ | Muss ein existierender Lieferant sein |
| `bestellmenge` | Integer | ✅ | Muss > 0 sein |
| `einkaufspreis` | Decimal | ❌ | Überschreibt `artikel.einkaufspreis` im PDF |
| `gewuenschtesLieferdatum` | Date | ❌ | Format: `YYYY-MM-DD` |
| `notiz` | String | ❌ | Freitext-Notiz |

**Response (201 Created):**

```json
{
  "bestellungId": 7,
  "bestellnummer": "BEST-2026-007",
  "pdfPfad": "Output/Orders/Bestellung_BEST-2026-007_2026-04-28.pdf",
  "xmlPfad": "Output/Orders/Bestellung_BEST-2026-007_2026-04-28.xml"
}
```

**`OrderService.erstelleManuelleBestellung()`-Ablauf:**

```
1. Lade Artikel via artikelId → EntityNotFoundException wenn nicht vorhanden
   → Prüfe: Artikel muss status=AKTIV haben (sonst BusinessException)
2. Lade Lieferant via lieferantId → EntityNotFoundException wenn nicht vorhanden
3. Generiere Bestellnummer: "BEST-{YYYY}-{NNN}" (gleiche Logik wie bestehende Methode)
4. Erstelle Bestellung:
   - artikel_id, lieferant_id aus Request
   - bestellmenge aus Request
   - einkaufspreis: aus Request falls angegeben, sonst artikel.einkaufspreis
   - gewuenschtes_lieferdatum, notiz aus Request (nullable)
   - status = 'OFFEN'
   - erstellt_von = SecurityUtils.getCurrentUsername()  ← Audit Trail
   - KEIN Bezug zu einem Bestellvorschlag (kein Status-Update auf bestellvorschlaege)
5. Speichere Bestellung via BestellungRepository
6. PdfGeneratorService.generate(bestellung) → PDF in Output/Orders/
7. XmlGeneratorService.generate(bestellung) → XML in Output/Orders/
8. Rückgabe: { bestellungId, bestellnummer, pdfPfad, xmlPfad }
```

**Wichtig – kein Seiteneffekt auf Bestellvorschläge:**  
Die Methode setzt **keinen** Bestellvorschlag auf `BESTELLT`. Sie erzeugt eine eigenständige Bestellung. Existierende Vorschläge für denselben Artikel bleiben im Status `VORSCHLAG` und erscheinen weiterhin im Dashboard.

## Akzeptanzkriterien

- [ ] `POST /api/v1/bestellungen` mit gültigem Request liefert `201 Created` + Response-Body mit Bestellnummer
- [ ] Bestellung wird in DB gespeichert mit `status=OFFEN`, ohne `vorschlag_id`-Bezug
- [ ] `erstellt_von` wird via `SecurityUtils.getCurrentUsername()` befüllt (Audit Trail)
- [ ] `einkaufspreis` aus Request hat Vorrang vor `artikel.einkaufspreis`; wenn beide fehlen: `null` in Bestellung
- [ ] PDF und XML werden in `Output/Orders/` generiert (gleiche Dateinamenskonvention wie bestehende Bestellungen)
- [ ] Request mit inaktivem Artikel → `422 Business Exception` mit verständlicher Fehlermeldung
- [ ] Request mit nicht existierendem `artikelId` oder `lieferantId` → `404 Not Found`
- [ ] Request ohne Pflichtfelder (`artikelId`, `lieferantId`, `bestellmenge`) → `400 Bad Request` mit Feldfehlern
- [ ] Bestehende Endpunkte `POST /api/v1/bestellungen/schnell/{vorschlagId}` und `POST /api/v1/bestellungen/bearbeitet/{vorschlagId}` sind unverändert funktionsfähig
- [ ] `OrderServiceTest` deckt manuelle Bestellung, Audit-Trail, inaktiver Artikel und unbekannte IDs ab
---
