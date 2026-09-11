# Setup: Datenbankschema

**Ziel**: Du verstehst das PostgreSQL-Datenbankschema und wie es beim Start initialisiert wird.

## Initialisierungsstrategie

Das Schema wird **nicht** durch Flyway oder Liquibase verwaltet. Spring Boot führt `backend/src/main/resources/schema.sql` bei jedem Start aus (`spring.sql.init.mode: always`). Alle `CREATE TABLE`-Statements nutzen `IF NOT EXISTS`, sodass kein Datenverlust bei Neustarts entsteht.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none   # Hibernate generiert kein Schema
  sql:
    init:
      mode: always     # schema.sql wird immer ausgeführt
```

## Tabellen-Übersicht

Die Tabellen werden in dieser Reihenfolge angelegt (wegen Fremdschlüssel-Abhängigkeiten):

```
lieferanten → artikel → bestellungen → bestellvorschlaege
                                     ↘ transaktionen
import_log (unabhängig)
```

## Tabellen im Detail

### `lieferanten`

Stammdaten der Lieferanten. Werden per CSV/Excel-Import befüllt.

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `lieferant_id` | `VARCHAR(50)` | `UNIQUE NOT NULL` |
| `name` | `VARCHAR(200)` | `NOT NULL` |
| `kontakt_email` | `VARCHAR(200)` | — |
| `kontakt_telefon` | `VARCHAR(50)` | — |
| `lead_time_tage` | `INTEGER` | `NOT NULL DEFAULT 1` |
| `erstellt_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

### `artikel`

Artikelstammdaten mit Bestandsinformationen und Disposition.

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `artikelnummer` | `VARCHAR(50)` | `UNIQUE NOT NULL` |
| `bezeichnung` | `VARCHAR(200)` | `NOT NULL` |
| `mengeneinheit` | `VARCHAR(20)` | `NOT NULL` |
| `warengruppe` | `VARCHAR(100)` | `NOT NULL` |
| `lieferant_id` | `BIGINT` | FK → `lieferanten(id)` |
| `aktueller_bestand` | `INTEGER` | `NOT NULL DEFAULT 0` |
| `sicherheitsbestand` | `INTEGER` | `NOT NULL DEFAULT 0` |
| `bestellpunkt` | `INTEGER` | `NOT NULL DEFAULT 0` |
| `standard_bestellmenge` | `INTEGER` | `NOT NULL DEFAULT 1` |
| `einkaufspreis` | `NUMERIC(10,2)` | — |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'AKTIV'` |
| `erstellt_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |
| `aktualisiert_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

### `bestellungen`

Bestellkopf für jede ausgelöste Bestellung (manuell oder via Vorschlag).

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `bestellnummer` | `VARCHAR(50)` | `UNIQUE NOT NULL` |
| `artikel_id` | `BIGINT` | `NOT NULL`, FK → `artikel(id)` |
| `lieferant_id` | `BIGINT` | `NOT NULL`, FK → `lieferanten(id)` |
| `bestellmenge` | `INTEGER` | `NOT NULL` |
| `einkaufspreis` | `NUMERIC(10,2)` | — |
| `gewuenschtes_lieferdatum` | `DATE` | — |
| `notiz` | `TEXT` | — |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'OFFEN'` |
| `erstellt_von` | `VARCHAR(100)` | `NOT NULL` (Audit-Trail) |
| `erstellt_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |
| `aktualisiert_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

### `bestellvorschlaege`

Vorschläge aus der automatischen Reorder-Analyse. Lifecycle: `VORSCHLAG` → `BESTELLT` / `IGNORIERT`.

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `artikel_id` | `BIGINT` | `NOT NULL`, FK → `artikel(id)` |
| `lieferant_id` | `BIGINT` | FK → `lieferanten(id)` |
| `bestellung_id` | `BIGINT` | FK → `bestellungen(id)` (gesetzt nach Bestellung) |
| `bestand_bei_erstellung` | `INTEGER` | `NOT NULL` |
| `vorgeschlagene_menge` | `INTEGER` | `NOT NULL` |
| `status` | `VARCHAR(20)` | `NOT NULL DEFAULT 'VORSCHLAG'` |
| `erstellt_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |
| `aktualisiert_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

### `transaktionen`

Alle Warenbewegungen (Eingang/Ausgang), manuell oder per Batch-Import gebucht.

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `artikel_id` | `BIGINT` | `NOT NULL`, FK → `artikel(id)` |
| `typ` | `VARCHAR(20)` | `NOT NULL` (`EINGANG` / `AUSGANG`) |
| `buchungstyp` | `VARCHAR(50)` | — (frei definierbar, z.B. `VERBRAUCH`) |
| `menge` | `INTEGER` | `NOT NULL` |
| `datum` | `DATE` | `NOT NULL` |
| `quelle` | `VARCHAR(20)` | `NOT NULL DEFAULT 'BATCH'` |
| `bestellung_id` | `BIGINT` | FK → `bestellungen(id)` |
| `lieferant_id` | `BIGINT` | FK → `lieferanten(id)` |
| `grund` | `TEXT` | — |
| `benutzer_id` | `VARCHAR(100)` | — (Audit-Trail bei manuellen Buchungen) |
| `erstellt_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

### `import_log`

Protokolliert jeden Datei-Import. Der SHA-256-Hash verhindert Doppelverarbeitung.

| Spalte | Typ | Constraints |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `dateiname` | `VARCHAR(500)` | `NOT NULL` |
| `datei_hash` | `VARCHAR(64)` | `UNIQUE NOT NULL` (SHA-256) |
| `typ` | `VARCHAR(50)` | `NOT NULL` |
| `status` | `VARCHAR(20)` | `NOT NULL` |
| `zeilen_gesamt` | `INTEGER` | — |
| `zeilen_erfolgreich` | `INTEGER` | — |
| `zeilen_fehlerhaft` | `INTEGER` | — |
| `fehler_details` | `TEXT` | — |
| `verarbeitet_am` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` |

## Direktzugriff auf die Datenbank (Entwicklung)

```bash
# psql-Shell im laufenden DB-Container öffnen
docker compose exec db psql -U lager -d lagerdb

# Tabellen auflisten
\dt

# Artikel anzeigen
SELECT artikelnummer, bezeichnung, aktueller_bestand FROM artikel;

# Container verlassen
\q
```
