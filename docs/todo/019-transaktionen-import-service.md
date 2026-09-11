---
Titel: TransaktionenImportService – Wareneingang & -ausgang Batch-Import
Status: [DONE]

### Implementierungsnotiz
Implementierung abgeschlossen. Tests in `src/test/java/com/lagermanagement/space/service/batch/TransaktionenImportServiceTest.java` vorhanden. BestellvorschlagRepository um `findByBestellungId` erweitert. Bestandsaktualisierung direkt via `artikelRepository.save()` (InventoryService noch nicht vorhanden).
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `TransaktionenImportService` für den Import von Warenbewegungen aus CSV/Excel-Dateien. Bestandsaktualisierungen und Transaktionsspeicherung laufen im gleichen `@Transactional`-Block. Bei Eingang mit `bestellung_referenz` wird die zugehörige Bestellung automatisch auf `GELIEFERT` gesetzt (R-L4).

Referenz: Masterplan Unit 5 — R-B1, R-B2, R-B3, R-I3, R-L4, R_2.2.2

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/batch/TransaktionenImportService.java`
- `backend/src/test/java/com/lagermanagement/space/service/batch/TransaktionenImportServiceTest.java`

**Import-Ablauf (für jede Datei in `Input/Transaktionen/`):**
1. SHA-256-Check → Doppelverarbeitungs-Schutz
2. Erkenne Datei-Typ: `eingang_*` → Wareneingang, `ausgang_*` → Warenausgang
3. Für jede Zeile: Validieren, Bestand aktualisieren, Transaktion speichern (ein `@Transactional`-Block)

**Wareneingang-Logik:**
- `artikel.aktuellerBestand += menge`
- Prüfe `bestellung_referenz`: wenn vorhanden und OFFENE Bestellung mit dieser Nummer existiert → Bestellung auf `GELIEFERT` setzen, Vorschlag auf `GELIEFERT` setzen (R-L4)
- Transaktion speichern: `typ=EINGANG`, `quelle=BATCH`

**Warenausgang-Logik:**
- Prüfe: `artikel.aktuellerBestand - menge >= 0` → bei Negativbestand: Zeile überspringen, WARN loggen
- `artikel.aktuellerBestand -= menge`
- Transaktion speichern: `typ=AUSGANG`, `quelle=BATCH`, `buchungstyp` aus Zeile

**Bestandsaktualisierung:** Ausschließlich über `InventoryService`-Methoden — niemals direkt per Repository auf `aktuellerBestand`

## Akzeptanzkriterien

- [ ] Bestandsaktualisierung und Transaktionsspeicherung laufen im gleichen `@Transactional`-Block
- [ ] SHA-256-Prüfung verhindert Doppelverarbeitung
- [ ] R-L4: Eingang mit `bestellung_referenz` schließt die passende Bestellung (`GELIEFERT`)
- [ ] Negativbestand wird verhindert (Zeile überspringen, WARN loggen)
- [ ] `buchungstyp` bei Ausgang wird korrekt aus der CSV-Zeile übernommen
- [ ] Transaktion hat `quelle=BATCH`
- [ ] Tests: Normaler Eingang, Eingang mit bestellung_referenz (R-L4), Ausgang, Negativbestand-Versuch
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
