# Architektur: Import-System

**Ziel**: Du verstehst, wie das automatisierte Import-System CSV- und Excel-Dateien verarbeitet und Doppelverarbeitungen verhindert.

## Überblick

Das Import-System lädt Stammdaten (Lieferanten, Artikel) und Transaktionen automatisch aus Dateien. Es läuft als **Scheduled Job** alle 15 Minuten (konfigurierbar). Ein SHA-256-Hash-Mechanismus verhindert, dass dieselbe Datei zweimal verarbeitet wird.

## Verzeichnisstruktur

```
files/
├── Input/
│   ├── Stammdaten/         # Eingangsordner für Lieferanten- & Artikel-Dateien
│   └── Transaktionen/      # Eingangsordner für Transaktionsdateien
└── Processed/
    ├── Stammdaten/
    │   ├── Success/YYYY-MM-DD/   # Erfolgreich verarbeitete Dateien
    │   ├── Warning/YYYY-MM-DD/   # Verarbeitet mit Warnungen (Skip-Zeilen)
    │   └── Error/YYYY-MM-DD/     # Fehlgeschlagene Dateien
    └── Transaktionen/
        ├── Success/YYYY-MM-DD/
        ├── Warning/YYYY-MM-DD/
        └── Error/YYYY-MM-DD/
```

## Ablauf (pro Datei)

```
1. Datei gefunden in Input/
2. SHA-256-Hash berechnen (FileService.computeHash)
3. Hash gegen import_log prüfen → bereits verarbeitet? → SKIP (Datei bleibt liegen)
4. Datei parsen (FileParser: CSV oder .xlsx)
5. Zeile für Zeile verarbeiten
   - Fehler in einer Zeile → WARN loggen, Zeile überspringen, weiter
6. Ergebnis in import_log speichern (Status: SUCCESS / WARNING / ERROR)
7. Datei verschieben:
   - Alle Zeilen OK → Processed/*/Success/
   - Zeilen mit Fehlern → Processed/*/Warning/
   - Datei komplett fehlerhaft → Processed/*/Error/
```

## Scheduler-Konfiguration

```yaml
app:
  import:
    cron-expression: "0 */15 * * * *"   # Alle 15 Minuten
```

Der `ImportScheduler` startet immer **Stammdaten zuerst**, dann Transaktionen – wegen der FK-Abhängigkeit (Artikel muss existieren, bevor Transaktionen dafür importiert werden).

```java
@Scheduled(cron = "${app.import.cron-expression}")
public void runImport() {
    stammdatenImportService.processAll();   // 1. Lieferanten & Artikel
    transaktionenImportService.processAll(); // 2. Transaktionen
}
```

## Duplikatschutz via SHA-256

Jede verarbeitete Datei hinterlässt einen Eintrag in der `import_log`-Tabelle:

```sql
datei_hash VARCHAR(64) UNIQUE NOT NULL
```

Wird eine bereits importierte Datei erneut in den Input-Ordner gelegt, erkennt das System den Hash und überspringt sie ohne Fehlermeldung. Das Verhalten ist idempotent.

## Unterstützte Dateiformate

| Format | Erkennung | Parser |
|---|---|---|
| `.csv` | Dateiendung | OpenCSV, Semikolon-Trennzeichen |
| `.xlsx` | Dateiendung | Apache POI |

Der `FileParser` erkennt das Format an der Dateiendung und wählt automatisch den richtigen Parser.

## Stammdaten-Import (StammdatenImportService)

Verarbeitet Dateien aus `Input/Stammdaten/`. Erwartet Spalten:

**Lieferanten** (`lieferanten_*.csv` / `.xlsx`):
- `lieferant_id`, `name`, `kontakt_email`, `kontakt_telefon`, `lead_time_tage`

**Artikel** (`artikel_*.csv` / `.xlsx`):
- `artikelnummer`, `bezeichnung`, `mengeneinheit`, `warengruppe`, `lieferant_id`, `sicherheitsbestand`, `bestellpunkt`, `standard_bestellmenge`, `einkaufspreis`

Bei bereits vorhandenen Datensätzen (Duplikat-Erkennung via `artikelnummer` / `lieferant_id`) wird **aktualisiert**, nicht neu angelegt (Upsert-Logik).

## Transaktionen-Import (TransaktionenImportService)

Verarbeitet Dateien aus `Input/Transaktionen/`. Erwartet Spalten:
- `artikelnummer`, `typ` (`EINGANG`/`AUSGANG`), `menge`, `datum` (`YYYY-MM-DD`), `buchungstyp`, `grund`

Pro importierter Transaktion wird der `aktueller_bestand` des Artikels atomisch aktualisiert.

## Fehlerbehandlung

Fehler in **einzelnen Zeilen** (z.B. unbekannte Artikelnummer) werden als `WARN` geloggt. Die Verarbeitung der Datei läuft weiter. Die fehlerhafte Zeile wird übersprungen.

Ein komplett fehlgeschlagener Import (z.B. korrupte Datei) wird als `ERROR` protokolliert. Die Datei landet im `Error`-Ordner.

```
[WARN] Zeile 15 übersprungen: Artikel 'ART-999' nicht gefunden
[ERROR] Datei 'transaktionen_2026-04.csv' konnte nicht geparst werden
```
