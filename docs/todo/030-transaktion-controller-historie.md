---
Titel: TransaktionController – Transaktionshistorie & Bestellhistorie
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `TransaktionController` für die paginierte, filterbare Transaktionshistorie (R-T1, R-T2) und die Bestellhistorie. Beide Endpunkte sind read-only und dienen ausschließlich der Anzeige.

Referenz: Masterplan Unit 8 — R-T1, R-T2, R_2.4.5

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/controller/TransaktionController.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/TransaktionDto.java` (Java Record)

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/v1/transaktionen` | Transaktionshistorie, paginiert + filterbar |
| `GET` | `/api/v1/bestellungen` | Alle Bestellungen, absteigend |
| `GET` | `/api/v1/bestellungen/{id}` | Einzelne Bestellung |

**Query-Parameter für `GET /api/v1/transaktionen`:**
- `artikelId` (optional) — Filter nach Artikel-ID
- `von` (optional, `YYYY-MM-DD`) — Startdatum
- `bis` (optional, `YYYY-MM-DD`) — Enddatum
- `seite` (default: 0) — Seitennummer für Paginierung
- `groesse` (default: 50) — Seitengröße

**TransaktionDto (Record):**
```java
public record TransaktionDto(Long id, String artikelnummer, String artikelBezeichnung,
    TransaktionTyp typ, String buchungstyp, int menge, LocalDate datum,
    TransaktionQuelle quelle, String benutzerId, String bestellnummer) {}
```

**Response:** `Page<TransaktionDto>` (Spring Pageable)

## Akzeptanzkriterien

- [ ] `GET /api/v1/transaktionen` unterstützt Filter nach `artikelId`, `von`, `bis`
- [ ] Paginierung ist implementiert (`seite`, `groesse` Parameter)
- [ ] `GET /api/v1/bestellungen` gibt alle Bestellungen in absteigender Reihenfolge zurück
- [ ] `GET /api/v1/bestellungen/{id}` gibt `404` bei unbekannter ID
- [ ] Keine Entities im Response — ausschließlich `TransaktionDto` und `BestellungDto`
- [ ] Alle Endpunkte sind read-only (nur GET)
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-transaktion.md
