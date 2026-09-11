---
Titel: schema.sql – vollständiges Datenbankschema erstellen
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle das vollständige PostgreSQL-Datenbankschema in `backend/src/main/resources/schema.sql`. Spring Boot führt diese Datei bei jedem Start aus (`spring.sql.init.mode=always`). Alle Tabellen nutzen `CREATE TABLE IF NOT EXISTS`, um Datenverlust bei Neustarts zu vermeiden.

Referenz: Masterplan Unit 2 — Abschnitt "Datenbankschema", R-D1

**Zu erstellende Datei:** `backend/src/main/resources/schema.sql`

**6 Tabellen (in dieser Reihenfolge wegen FK-Abhängigkeiten):**

1. `lieferanten` — Lieferantenstammdaten
2. `artikel` — Artikelstammdaten mit FK zu `lieferanten`
3. `bestellungen` — Bestellkopf mit FK zu `artikel` und `lieferanten`
4. `bestellvorschlaege` — Vorschlags-Lifecycle mit FK zu `artikel`, `lieferanten`, `bestellungen`
5. `transaktionen` — Warenbewegungen mit FK zu `artikel`, `bestellungen`, `lieferanten`
6. `import_log` — SHA-256-basierter Doppelverarbeitungs-Schutz

**Kritische Constraints:**
- `artikel.artikelnummer` — `UNIQUE NOT NULL`
- `lieferanten.lieferant_id` — `UNIQUE NOT NULL`
- `bestellungen.bestellnummer` — `UNIQUE NOT NULL`
- `import_log.datei_hash` — `UNIQUE NOT NULL` (SHA-256-Prüfung)
- Status-Felder als `VARCHAR` mit Default-Werten (keine DB-Enums, da Spring Enums als String speichert)

**Optional:** `backend/src/main/resources/data.sql` mit einem Test-Lieferanten und einem Test-Artikel für Entwicklungszwecke.

## Akzeptanzkriterien

- [x] Alle 6 Tabellen vorhanden: `lieferanten`, `artikel`, `bestellungen`, `bestellvorschlaege`, `transaktionen`, `import_log`
- [x] Alle Statements nutzen `CREATE TABLE IF NOT EXISTS`
- [x] FK-Reihenfolge korrekt (lieferanten → artikel → bestellungen → bestellvorschlaege/transaktionen)
- [x] UNIQUE-Constraints: `artikelnummer`, `lieferant_id`, `bestellnummer`, `datei_hash`
- [x] Alle Spalten aus dem Masterplan sind vorhanden (inkl. Audit-Felder `erstellt_von`, `benutzer_id`)
- [ ] Tabellen-Prüfung via psql nach erstem Start (Integrationstest ausstehend)

Implementierung abgeschlossen.
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-datenbank.md
