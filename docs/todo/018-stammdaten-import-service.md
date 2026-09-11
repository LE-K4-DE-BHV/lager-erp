---
Titel: StammdatenImportService – Artikel & Lieferanten-Import
Status: [DONE]

### Implementierungsnotiz
Implementierung abgeschlossen. Tests in `src/test/java/com/lagermanagement/space/service/batch/StammdatenImportServiceTest.java` vorhanden.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `StammdatenImportService` für den Import von Artikel- und Lieferanten-Stammdaten aus CSV/Excel-Dateien. Der Service folgt dem Standard-Import-Ablauf: SHA-256-Prüfung → Parsen → Validieren → Persistieren → Verschieben. Lieferanten werden immer vor Artikeln verarbeitet (FK-Abhängigkeit).

Referenz: Masterplan Unit 5 — R-B1, R-B2, R-B3, R-I1, R-I2, R_2.1.3

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/batch/StammdatenImportService.java`
- `backend/src/test/java/com/lagermanagement/space/service/batch/StammdatenImportServiceTest.java`

**Import-Ablauf (für jede Datei in `Input/Stammdaten/`):**
1. Berechne SHA-256-Hash
2. Prüfe `ImportLogRepository.existsByDateiHash(hash)` → wenn bereits vorhanden: überspringen (LOG INFO)
3. Parse Datei via `FileParser`
4. Erkenne Datei-Typ anhand Präfix: `lieferanten_*` → Lieferanten-Import, `artikel_*` → Artikel-Import
5. **Lieferanten zuerst verarbeiten** (FK-Abhängigkeit)
6. Für jede Zeile: Validieren und persistieren
   - Fehler pro Zeile: überspringen, `fehlerhafteZeilen++`, Fehler als WARN loggen
7. Klassifiziere Ergebnis: SUCCESS / WARNING / ERROR
8. Verschiebe Datei in korrekten `Processed/`-Ordner
9. Speichere `ImportLog`-Eintrag

**Upsert-Logik (Artikel):**
- `artikelnummer` bereits vorhanden → UPDATE (kein Fehler)
- `lieferant_id` in Zeile nicht in DB → Zeile überspringen, Fehler loggen

**Upsert-Logik (Lieferanten):**
- `lieferant_id` bereits vorhanden → UPDATE

## Akzeptanzkriterien

- [ ] Lieferanten werden vor Artikeln verarbeitet (auch wenn beide Dateitypen im gleichen Lauf vorliegen)
- [ ] SHA-256-Prüfung verhindert Doppelverarbeitung
- [ ] Fehlende `lieferant_id` bei Artikel-Zeile → Zeile überspringen, Warnung loggen
- [ ] Bei teilweise fehlerhaften Dateien: Status `WARNING`, Datei in `Processed/Warning/YYYY-MM-DD/`
- [ ] Bei vollständig fehlerhafte Dateien (Parse-Fehler): Status `ERROR`, Datei in `Processed/Error/`
- [ ] `ImportLog`-Eintrag wird in jedem Fall gespeichert (auch bei ERROR)
- [ ] `@Transactional` ist auf Service-Methoden gesetzt, die Daten schreiben
- [ ] Tests: Erfolg, Doppelverarbeitung, Teilfehler, Komplett-Fehler abgedeckt
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
