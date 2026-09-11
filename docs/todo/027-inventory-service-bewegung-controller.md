---
Titel: InventoryService + BewegungController – manuelle Lagerbewegungen
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `InventoryService` für transaktionssichere manuelle Lagerbewegungen (Wareneingang + Warenausgang) und den zugehörigen `BewegungController`. Alle Bestandsänderungen laufen ausschließlich über diesen Service — direkte Repository-Updates auf `aktuellerBestand` sind verboten. `@Lock(PESSIMISTIC_WRITE)` verhindert Race-Conditions.

Referenz: Masterplan Unit 8 — R-M1, R-M2, R-M3, R-M4, R-M5

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/InventoryService.java`
- `backend/src/main/java/com/lagermanagement/space/web/controller/BewegungController.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/EingangRequest.java` (Record)
- `backend/src/main/java/com/lagermanagement/space/web/dto/AusgangRequest.java` (Record)
- `backend/src/test/java/com/lagermanagement/space/service/InventoryServiceTest.java`

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `POST` | `/api/v1/bewegungen/eingang` | Wareneingang buchen |
| `POST` | `/api/v1/bewegungen/eingang/{transaktionId}/bestellung-abschliessen` | Bestellung als geliefert markieren |
| `POST` | `/api/v1/bewegungen/ausgang` | Warenausgang buchen |

**InventoryService.bucheEingang()-Ablauf:**
1. Lade Artikel → `EntityNotFoundException` wenn nicht vorhanden
2. Prüfe offene Bestellung: `BestellungRepository.findAllByArtikelIdAndStatus(artikelId, "OFFEN")`
   → wenn leer: `throw BusinessException("Keine offene Bestellung für diesen Artikel")`
3. `artikel.aktuellerBestand += menge` (innerhalb `@Transactional`)
4. Speichere Transaktion (`quelle=MANUELL`, `typ=EINGANG`, `benutzerId=SecurityUtils.getCurrentUsername()`)
5. Rückgabe: `{ transaktionId, bestellungKannGeschlossenWerden: true }`

**InventoryService.bucheAusgang()-Ablauf:**
1. Prüfe Negativbestand: wenn `bestand - menge < 0` → `BusinessException`
2. `artikel.aktuellerBestand -= menge`
3. Speichere Transaktion (`quelle=MANUELL`, `typ=AUSGANG`, `buchungstyp` aus Request)
4. Audit Trail: `benutzerId=SecurityUtils.getCurrentUsername()`

**`@Lock(PESSIMISTIC_WRITE)`** auf Artikel-Repository-Abfrage im InventoryService (verhindert Race-Conditions bei parallelen Buchungen).

## Akzeptanzkriterien

- [ ] `bucheEingang()` wirft `BusinessException` wenn keine offene Bestellung existiert
- [ ] `bucheAusgang()` verhindert Negativbestand (`BusinessException` bei `menge > bestand`)
- [ ] Beide Methoden sind `@Transactional`
- [ ] `benutzerId` in Transaktion wird via `SecurityUtils.getCurrentUsername()` gesetzt (Audit Trail)
- [ ] `@Lock(PESSIMISTIC_WRITE)` ist auf der Artikel-Abfrage im InventoryService gesetzt
- [ ] Direkte Repository-Updates auf `aktuellerBestand` existieren nirgendwo außer im InventoryService
- [ ] `InventoryServiceTest` deckt Eingang-Erfolg, Eingang-ohne-Bestellung, Ausgang-Erfolg, Negativbestand ab
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-lagerbewegung.md
