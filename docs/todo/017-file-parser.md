---
Titel: FileParser – CSV- und Excel-Parsing
Status: [DONE]

### Implementierungsnotiz
Implementierung abgeschlossen. `ParseException.java` (eigene checked Exception) und `FileParser.java` in `service/batch/` erstellt.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `FileParser` als Utility-Klasse für das Einlesen von CSV- und Excel-Dateien. Der Parser gibt eine normalisierte Liste von Maps (Spaltenkopf → Wert) zurück, sodass alle Import-Services dieselbe Datenstruktur erhalten.

Referenz: Masterplan Unit 5 — Abschnitt "FileParser"

**Zu erstellende Datei:**
- `backend/src/main/java/com/lagermanagement/space/service/batch/FileParser.java`

**Schnittstelle:**
```java
// Erkennt Format anhand Dateiendung (.csv oder .xlsx)
// Gibt normalisierte Datenzeilen zurück (Schlüssel: lowercase Spaltenköpfe)
List<Map<String, String>> parse(Path file) throws ParseException
```

**CSV-Parsing:**
- Nutzt OpenCSV (`com.opencsv`)
- Spaltenköpfe werden auf lowercase normalisiert (case-insensitive Matching — R-I3)
- Leere Zeilen werden übersprungen

**Excel-Parsing:**
- Nutzt Apache POI (`poi-ooxml`)
- Erste Zeile = Spaltenköpfe (lowercase normalisiert)
- Alle Zelltypen werden als String gelesen

**Pflichtfelder-Validierung:**
- Der Parser prüft, ob alle Pflicht-Spalten vorhanden sind
- Fehlen Pflicht-Spalten → `ParseException` wird geworfen → Datei wird als ERROR klassifiziert

**Import-Schemas (verbindliche Pflicht-Spalten):**
- Stammdaten-Artikel: `artikelnummer`, `bezeichnung`, `mengeneinheit`, `warengruppe`, `lieferant_id`
- Stammdaten-Lieferanten: `lieferant_id`, `name`, `lead_time_tage`
- Transaktionen-Eingang: `datum`, `artikelnummer`, `menge`
- Transaktionen-Ausgang: `datum`, `artikelnummer`, `menge`, `buchungstyp`

## Akzeptanzkriterien

- [ ] `parse()` erkennt Format anhand Dateiendung (`.csv` vs. `.xlsx`)
- [ ] Spaltenköpfe werden auf lowercase normalisiert
- [ ] Fehlende Pflicht-Spalten werfen eine `ParseException` (Datei → ERROR-Ordner)
- [ ] Leere Datenzeilen werden übersprungen
- [ ] Excel-Zellen verschiedener Typen (Zahl, Text, Datum) werden korrekt als String extrahiert
- [ ] Der Parser selbst ändert keine DB-Daten — reine Read-Only-Utility-Klasse
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
