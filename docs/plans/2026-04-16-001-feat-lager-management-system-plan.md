---

## Title: "feat: Interaktives Lager-Management-System"  
type: feat  
status: active  
date: 2026-04-16  
origin: docs/brainstorms/2026-04-14-lager-optimierung-requirements.md

# feat: Interaktives Lager-Management-System (Fullstack)

## Übersicht

Dieses Dokument ist der verbindliche Implementierungsplan für das Lager-Management-System. Es beschreibt **was** gebaut wird, **wie** die einzelnen Komponenten zusammenspielen und in welcher **Reihenfolge** vorgegangen wird. Es enthält keinen fertigen Code – sondern klare Entscheidungen, Dateistrukturen, Schemas und Testszenarien, die einen sofortigen Start der Programmierung ermöglichen.

**Projektziel:** Containerisierte Fullstack-Webanwendung (Spring Boot + Vue.js 3 + PostgreSQL), die datengetriebene Bestellvorschläge generiert und den Menschen als finalen Entscheider behält ("Human-in-the-Loop").

---

## Problem Frame

Der manuelle Lagerprozess bindet unnötig Kapital und erzeugt Fehlbestellungsrisiken. Das System automatisiert die Analyse, überlässt aber die Freigabe dem Nutzer. Gleichzeitig dient das Projekt als Fullstack-Portfolio-Nachweis (Java + Vue.js + DevOps).

> Weitere Details: (see origin: `docs/brainstorms/2026-04-14-lager-optimierung-requirements.md`)

---

## Requirements Trace


| ID  | Anforderung                                                                                                                                |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| R-A | Spring Security HTTP Basic Auth, 1 User `operator`, httpOnly Session-Cookie, kein HTTPS                                                    |
| R-B | `@Scheduled` Batch-Import: Scan → Parse → Validate → Persist → Move. Skip bei Zeilenfehler. Ordner: Success/Warning/Error                  |
| R-D | Schema via `schema.sql`, kein Flyway. `spring.jpa.hibernate.ddl-auto=none`                                                                 |
| R-I | 4 feste Datei-Schemas (Stammdaten-Artikel, Stammdaten-Lieferanten, Transaktionen-Eingang, Transaktionen-Ausgang) mit exakten Spaltenköpfen |
| R-L | Bestellvorschlag-Lebenszyklus: VORSCHLAG → BESTELLT → GELIEFERT (oder IGNORIERT)                                                           |
| R-M | Manuelle Lagerbewegungen (Eingang nur wenn offene Bestellung existiert; Ausgang mit Buchungstyp)                                           |
| R-N | Artikel anlegen/bearbeiten per UI; Lieferanten nur via Batch anlegen, per UI bearbeiten                                                    |
| R-O | Bestellausgabe als PDF (OpenPDF) + XML (JAXB) in `Output/Orders/`                                                                          |
| R-P | Dashboard als Hub mit KPI-Summary, Quick-Links, manuellem Reorder-Trigger                                                                  |
| R-S | Artikel haben `einkaufspreis`, `sicherheitsbestand`, `bestellpunkt` (manuell gepflegt)                                                     |
| R-T | Transaktionshistorie-Seite mit Filter nach Artikel und Zeitraum                                                                            |
| R-V | Lieferantenliste mit Bearbeitungsmöglichkeit per UI                                                                                        |


---

## Scope Boundaries

- **Kein** Multi-User / Rollen / Passwort-Reset
- **Kein** Spring Batch (nur `@Scheduled` + Services)
- **Kein** Flyway / Liquibase
- **Kein** Message-Broker (Kafka, RabbitMQ)
- **Kein** WebSocket / Real-Time Push
- **Keine** automatischen Bestellungen – immer manuelle Freigabe
- **Kein** physisches Löschen von Artikeln – nur `INAKTIV`-Status
- **Keine** Teillieferungen – Wareneingang schließt Bestellung vollständig
- **TLS/HTTPS** ausgeschlossen (lokaler/interner Betrieb)

---

## Kontext & Forschung

### Vorhandene Projektstruktur

Das Projekt ist ein greenfield Spring Boot Skeleton:

```
src/main/java/com/lagermanagement/space/SpaceApplication.java
src/main/resources/application.properties   ← wird zu application.yml umgebaut
src/test/java/com/lagermanagement/space/SpaceApplicationTests.java
.mvn/wrapper/maven-wrapper.properties
```

Kein Frontend, keine Entities, keine Konfiguration vorhanden.

### Ziel-Verzeichnisstruktur (Gesamt)

```
lager_management/
├── backend/                         ← Spring Boot Modul
│   ├── src/main/java/com/lagermanagement/space/
│   │   ├── config/
│   │   ├── domain/entity/
│   │   ├── domain/repository/
│   │   ├── service/
│   │   ├── service/batch/
│   │   ├── web/controller/
│   │   └── web/dto/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── schema.sql
│   │   └── data.sql (optional)
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                        ← Vue.js 3 Modul
│   ├── src/
│   │   ├── main.js
│   │   ├── App.vue
│   │   ├── router/index.js
│   │   ├── api/axios.js
│   │   ├── api/services/
│   │   ├── views/
│   │   └── components/
│   ├── nginx.conf
│   ├── Dockerfile
│   └── package.json / vite.config.js
├── files/
│   ├── Input/Stammdaten/
│   ├── Input/Transaktionen/
│   ├── Processed/Success/
│   ├── Processed/Warning/
│   ├── Processed/Error/
│   ├── Output/Reports/
│   └── Output/Orders/
├── docker-compose.yml
└── .env                             ← nicht in Git
```

> **Hinweis zur aktuellen Lage:** Die Dateien `SpaceApplication.java` und `application.properties` befinden sich direkt in `src/`, nicht in einem `backend/`-Unterordner. Es gibt zwei Optionen:
>
> - **Option A (empfohlen):** Gesamte Struktur in `backend/` und `frontend/` aufteilen – sauberer für Docker.
> - **Option B:** Backend bleibt im Projektwurzel, `frontend/` wird als Unterordner hinzugefügt.
>
> **Entscheidung: Option A.** Das Projekt wird in `backend/` und `frontend/` aufgeteilt. Die bestehenden Dateien werden entsprechend verschoben.

---

## Gelöste Planungsfragen (Deferred from Requirements)


| Frage                                  | Entscheidung                                                                                                                                                                                                                                                                                                                                       |
| -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Nginx Reverse Proxy Konfiguration      | Alle Anfragen an `/api/`** werden von Nginx an `http://backend:8080` weitergeleitet. Vue.js SPA wird auf dem gleichen Nginx-Container als statische Dateien bereitgestellt. Kein CORS-Problem (Same-Origin).                                                                                                                                       |
| Verbrauchsanalysezeitraum              | Standard: **30 Tage**. Konfigurierbar via `app.reorder.consumption-period-days=30` in `application.yml`.                                                                                                                                                                                                                                           |
| Doppelverarbeitung verhindern          | Datenbank-Tabelle `import_log` mit SHA-256-Hash der Datei. Vor Verarbeitung wird geprüft, ob der Hash bereits existiert. Wenn ja → überspringen + Info-Log.                                                                                                                                                                                        |
| IGNORIERT-Mechanismus                  | Im Reorder-Job: Wenn für einen Artikel bereits ein Eintrag mit `status = 'IGNORIERT'` existiert UND `aktueller_bestand > sicherheitsbestand`, wird **kein neuer** Vorschlag erstellt. Sobald `aktueller_bestand <= sicherheitsbestand` (kritisch), wird trotzdem ein neuer Vorschlag erstellt – der kritische Zustand überschreibt das Ignorieren. |
| Manueller Reorder-Trigger (sync/async) | **Synchron.** Der Job ist für das Datenvolumen schnell genug. Das Frontend zeigt einen Lade-Spinner und wartet auf die Antwort (max. einige Sekunden).                                                                                                                                                                                             |
| Verarbeitete Dateien – Ordnerstruktur  | Dateien werden nach Datum gruppiert: `Processed/{Success                                                                                                                                                                                                                                                                                           |


---

## Datei-Import-Schemas (Verbindlich)

> Diese Schemas sind die technische Grundlage für den Batch-Import (R-I1, R-I2, R-I3). Spaltenköpfe sind case-insensitive.

### Schema 1: Stammdaten-Artikel (`Input/Stammdaten/artikel_*.csv` oder `.xlsx`)


| Spaltenname             | Typ     | Pflicht | Beschreibung                                 |
| ----------------------- | ------- | ------- | -------------------------------------------- |
| `artikelnummer`         | String  | ✅       | Eindeutige Artikelnummer                     |
| `bezeichnung`           | String  | ✅       | Artikelbezeichnung                           |
| `mengeneinheit`         | String  | ✅       | z.B. `Stück`, `kg`, `Liter`                  |
| `warengruppe`           | String  | ✅       | z.B. `Elektronik`, `Verbrauchsmaterial`      |
| `lieferant_id`          | String  | ✅       | Muss in `lieferanten`-Tabelle existieren     |
| `sicherheitsbestand`    | Integer | ❌       | Default: 0                                   |
| `bestellpunkt`          | Integer | ❌       | Default: 0                                   |
| `standard_bestellmenge` | Integer | ❌       | Default: 1                                   |
| `einkaufspreis`         | Decimal | ❌       | Preis pro Mengeneinheit, Dezimaltrenner: `.` |


**Beispiel-CSV:**

```csv
artikelnummer,bezeichnung,mengeneinheit,warengruppe,lieferant_id,sicherheitsbestand,bestellpunkt,standard_bestellmenge,einkaufspreis
ART-001,Schraube M6x20,Stück,Schrauben,LF-001,100,250,1000,0.05
ART-002,Hydrauliköl HLP 46,Liter,Schmierstoffe,LF-002,20,50,200,3.50
```

---

### Schema 2: Stammdaten-Lieferanten (`Input/Stammdaten/lieferanten_*.csv` oder `.xlsx`)


| Spaltenname       | Typ     | Pflicht | Beschreibung                          |
| ----------------- | ------- | ------- | ------------------------------------- |
| `lieferant_id`    | String  | ✅       | Eindeutige Lieferanten-ID             |
| `name`            | String  | ✅       | Firmenname                            |
| `kontakt_email`   | String  | ❌       | E-Mail-Adresse                        |
| `kontakt_telefon` | String  | ❌       | Telefonnummer                         |
| `lead_time_tage`  | Integer | ✅       | Durchschnittliche Lieferzeit in Tagen |


**Beispiel-CSV:**

```csv
lieferant_id,name,kontakt_email,kontakt_telefon,lead_time_tage
LF-001,Schrauben GmbH,einkauf@schrauben.de,+49 221 12345,5
LF-002,Öl & Schmierstoff AG,bestellung@oel-ag.de,,10
```

---

### Schema 3: Transaktionen-Eingang (`Input/Transaktionen/eingang_*.csv` oder `.xlsx`)


| Spaltenname           | Typ               | Pflicht | Beschreibung                           |
| --------------------- | ----------------- | ------- | -------------------------------------- |
| `datum`               | Date `YYYY-MM-DD` | ✅       | Datum des Wareneingangs                |
| `artikelnummer`       | String            | ✅       | Referenz auf Artikel                   |
| `menge`               | Integer           | ✅       | Eingegangene Menge (positiv)           |
| `lieferant_id`        | String            | ❌       | Optionale Lieferantenreferenz          |
| `bestellung_referenz` | String            | ❌       | Bestellnummer für Auto-Matching (R-L4) |


**Beispiel-CSV:**

```csv
datum,artikelnummer,menge,lieferant_id,bestellung_referenz
2026-04-10,ART-001,1000,LF-001,BEST-2026-042
2026-04-11,ART-002,200,LF-002,
```

---

### Schema 4: Transaktionen-Ausgang (`Input/Transaktionen/ausgang_*.csv` oder `.xlsx`)


| Spaltenname     | Typ               | Pflicht | Beschreibung                                                           |
| --------------- | ----------------- | ------- | ---------------------------------------------------------------------- |
| `datum`         | Date `YYYY-MM-DD` | ✅       | Datum des Warenausgangs                                                |
| `artikelnummer` | String            | ✅       | Referenz auf Artikel                                                   |
| `menge`         | Integer           | ✅       | Ausgehende Menge (positiv)                                             |
| `buchungstyp`   | String            | ✅       | Einer von: `Verbrauch intern`, `Verkauf`, `Verlust/Schwund`, `Retoure` |
| `grund`         | String            | ❌       | Optionaler Freitext-Grund                                              |


**Beispiel-CSV:**

```csv
datum,artikelnummer,menge,buchungstyp,grund
2026-04-12,ART-001,50,Verbrauch intern,Montagelinie 3
2026-04-12,ART-002,5,Verlust/Schwund,Behälter beschädigt
```

---

## Datenbankschema

> Das vollständige Schema wird in `src/main/resources/schema.sql` gepflegt.  
> `spring.sql.init.mode=always` + `spring.jpa.hibernate.ddl-auto=none` in `application.yml`.

```sql
-- Datei: src/main/resources/schema.sql

CREATE TABLE IF NOT EXISTS lieferanten (
    id              BIGSERIAL PRIMARY KEY,
    lieferant_id    VARCHAR(50) UNIQUE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    kontakt_email   VARCHAR(200),
    kontakt_telefon VARCHAR(50),
    lead_time_tage  INTEGER NOT NULL DEFAULT 1,
    erstellt_am     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS artikel (
    id                    BIGSERIAL PRIMARY KEY,
    artikelnummer         VARCHAR(50) UNIQUE NOT NULL,
    bezeichnung           VARCHAR(200) NOT NULL,
    mengeneinheit         VARCHAR(20) NOT NULL,
    warengruppe           VARCHAR(100) NOT NULL,
    lieferant_id          BIGINT REFERENCES lieferanten(id),
    aktueller_bestand     INTEGER NOT NULL DEFAULT 0,
    sicherheitsbestand    INTEGER NOT NULL DEFAULT 0,
    bestellpunkt          INTEGER NOT NULL DEFAULT 0,
    standard_bestellmenge INTEGER NOT NULL DEFAULT 1,
    einkaufspreis         NUMERIC(10,2),
    status                VARCHAR(20) NOT NULL DEFAULT 'AKTIV',  -- AKTIV | INAKTIV
    erstellt_am           TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bestellungen (
    id                      BIGSERIAL PRIMARY KEY,
    bestellnummer           VARCHAR(50) UNIQUE NOT NULL,
    artikel_id              BIGINT NOT NULL REFERENCES artikel(id),
    lieferant_id            BIGINT NOT NULL REFERENCES lieferanten(id),
    bestellmenge            INTEGER NOT NULL,
    einkaufspreis           NUMERIC(10,2),
    gewuenschtes_lieferdatum DATE,
    notiz                   TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OFFEN',  -- OFFEN | GELIEFERT
    erstellt_von            VARCHAR(100) NOT NULL,
    erstellt_am             TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bestellvorschlaege (
    id                   BIGSERIAL PRIMARY KEY,
    artikel_id           BIGINT NOT NULL REFERENCES artikel(id),
    lieferant_id         BIGINT REFERENCES lieferanten(id),
    bestellung_id        BIGINT REFERENCES bestellungen(id),
    bestand_bei_erstellung INTEGER NOT NULL,
    vorgeschlagene_menge INTEGER NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'VORSCHLAG',  -- VORSCHLAG | BESTELLT | GELIEFERT | IGNORIERT
    erstellt_am          TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transaktionen (
    id           BIGSERIAL PRIMARY KEY,
    artikel_id   BIGINT NOT NULL REFERENCES artikel(id),
    typ          VARCHAR(20) NOT NULL,   -- EINGANG | AUSGANG
    buchungstyp  VARCHAR(50),            -- WARENEINGANG | Verbrauch intern | Verkauf | Verlust/Schwund | Retoure
    menge        INTEGER NOT NULL,
    datum        DATE NOT NULL,
    quelle       VARCHAR(20) NOT NULL DEFAULT 'BATCH',  -- BATCH | MANUELL
    bestellung_id BIGINT REFERENCES bestellungen(id),
    lieferant_id BIGINT REFERENCES lieferanten(id),
    grund        TEXT,
    benutzer_id  VARCHAR(100),
    erstellt_am  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS import_log (
    id                 BIGSERIAL PRIMARY KEY,
    dateiname          VARCHAR(500) NOT NULL,
    datei_hash         VARCHAR(64) NOT NULL UNIQUE,   -- SHA-256
    typ                VARCHAR(50) NOT NULL,            -- STAMMDATEN_ARTIKEL | STAMMDATEN_LIEFERANTEN | TRANSAKTIONEN_EINGANG | TRANSAKTIONEN_AUSGANG
    status             VARCHAR(20) NOT NULL,            -- SUCCESS | WARNING | ERROR
    zeilen_gesamt      INTEGER,
    zeilen_erfolgreich INTEGER,
    zeilen_fehlerhaft  INTEGER,
    fehler_details     TEXT,
    verarbeitet_am     TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## Schlüsseltechnische Entscheidungen


| Entscheidung        | Wahl                                                                | Begründung                                                                                                            |
| ------------------- | ------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Projektstruktur     | Monorepo: `backend/` + `frontend/`                                  | Klare Trennung, einfaches Docker-Compose-Orchestrieren                                                                |
| Paketname           | `com.lagermanagement.space`                                         | Bestehende Struktur beibehalten                                                                                       |
| Bestand-Tracking    | `aktueller_bestand` direkt in `artikel`-Tabelle                     | Denormalisiert, aber transaktionssicher via `@Transactional`. Einfacher als separate Bestandstabelle für diesen Scope |
| API-Basis-Pfad      | `/api/v1/`                                                          | Versionierung von Anfang an, Nginx-Proxy leitet weiter                                                                |
| DTOs vs. Entities   | Separate DTOs für alle API-Responses                                | Kein versehentliches Exponieren von DB-Internals (Lazy-Loading, Zirkelreferenzen)                                     |
| Error Handling      | `@ControllerAdvice` mit strukturierten JSON-Fehlern                 | Konsistentes Fehlerformat für das Frontend                                                                            |
| PDF-Generierung     | OpenPDF (LGPL)                                                      | Lizenzfrei, ausreichend für Portfolio                                                                                 |
| XML-Generierung     | JAXB + separate `BestellungXml`-POJO-Klassen                        | Saubere Serialisierung, keine Entity-Verschmutzung                                                                    |
| Session Management  | Spring Security `HttpSession` + `httpOnly`-Cookie                   | XSS-sicher, kein Token-Management im Frontend                                                                         |
| Nginx-Konfiguration | Alle `/api/**` → Proxy zu `backend:8080`; alles andere → Vue.js SPA | Same-Origin, kein CORS                                                                                                |


---

## High-Level Technical Design

> *Diese Grafik illustriert den beabsichtigten Ansatz und ist als Orientierung gedacht, nicht als Implementierungsspezifikation.*

### Komponenteninteraktion

```
┌─────────────────── Docker Compose ───────────────────────────────────┐
│                                                                       │
│  Browser                                                              │
│    │                                                                  │
│    ▼                                                                  │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  frontend (nginx:alpine)  Port 80                               │  │
│  │  ┌──────────────────────┐   ┌──────────────────────────────────┐│  │
│  │  │  Vue.js SPA (static) │   │  Reverse Proxy: /api/** → :8080 ││  │
│  │  └──────────────────────┘   └──────────────────────────────────┘│  │
│  └────────────────────────────────────────┬────────────────────────┘  │
│                                           │ HTTP (internes Netz)      │
│  ┌────────────────────────────────────────▼────────────────────────┐  │
│  │  backend (Spring Boot)  Port 8080                               │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────────┐ │  │
│  │  │ Security │ │REST API  │ │ Batch    │ │ Output Services    │ │  │
│  │  │ Config   │ │ /api/v1/ │ │ Services │ │ PDF / XML / Mail   │ │  │
│  │  └──────────┘ └──────────┘ └────┬─────┘ └────────────────────┘ │  │
│  └─────────────────────────────────┼────────────────────────────────┘  │
│                                    │ JPA                              │
│  ┌─────────────────────────────────▼────────────────────────────────┐  │
│  │  db (postgres:16-alpine)  Port 5432                              │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  ./files Volume (Host-Mount)                                     │  │
│  │  Input/ → Processed/ → Output/                                   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
```

### Bestellvorschlag-Lebenszyklus

```
@Scheduled-Job läuft (nächtlich oder manuell getriggert via /api/v1/reorder/trigger)
        │
        ▼
Für jeden AKTIVEN Artikel:
  aktueller_bestand <= bestellpunkt?  
  UND kein offener VORSCHLAG/BESTELLT-Eintrag?
  UND (kein IGNORIERT-Eintrag ODER Bestand <= sicherheitsbestand)?
        │ JA
        ▼
  Neuer Bestellvorschlag (status='VORSCHLAG') in DB

        │
        ▼ Dashboard lädt GET /api/v1/dashboard/vorschlaege
        │
   ┌────┼─────────────────────┐
   ▼    ▼                     ▼
[Schnell-  [Bearbeiten &   [Ignorieren]
bestellen]  Bestellen]          │
   │           │               │
   └─────┬─────┘         status='IGNORIERT'
         ▼
   POST /api/v1/bestellungen
   → status='BESTELLT'
   → PDF + XML generiert
   → Vorschlag bleibt als "In Lieferung" sichtbar
         │
         ▼ Wareneingang (manuell oder Batch)
         │
   status='GELIEFERT' → verschwindet aus Dashboard
```

---

## Implementierungseinheiten

### Phase 1: Infrastruktur & Projektkonfiguration

---

- **Unit 1: Docker Compose, Dockerfiles & Nginx**

**Ziel:** Lauffähige Container-Infrastruktur für Backend, Frontend und Datenbank.

**Requirements:** R_3.1.2, R_3.1.3, R-A4

**Abhängigkeiten:** Keine – dieser Unit kommt zuerst.

**Dateien:**

- Erstellen: `docker-compose.yml`
- Erstellen: `.env` (Template, nicht in Git → in `.gitignore`)
- Erstellen: `backend/Dockerfile`
- Erstellen: `frontend/Dockerfile`
- Erstellen: `frontend/nginx.conf`
- Erstellen: `.gitignore`

**Ansatz:**

*docker-compose.yml* definiert drei Services: `db`, `backend`, `frontend`.

- `db`: Image `postgres:16-alpine`, Umgebungsvariablen aus `.env`, Named Volume `db_data`.
- `backend`: Eigenes Dockerfile (Multi-Stage: Maven Build → Java 25 slim Runtime). Depends on `db`. Mount `./files:/app/files`. Ports: `8080:8080`. Alle sensitiven Werte (`APP_OPERATOR_PASSWORD_HASH`, `DB_PASSWORD`, SMTP-Vars) kommen aus `.env`.
- `frontend`: Eigenes Dockerfile (Multi-Stage: Node Build → nginx:alpine Runtime). Ports: `80:80`. Beinhaltet `nginx.conf` mit dem Reverse-Proxy zu `backend:8080`.

*backend/Dockerfile:*

- Stage 1: `maven:3.9-eclipse-temurin-21` → `mvn package -DskipTests`
- Stage 2: `eclipse-temurin:21-jre-alpine` → kopiert JAR, `EXPOSE 8080`, `ENTRYPOINT`

*frontend/Dockerfile:*

- Stage 1: `node:20-alpine` → `npm install && npm run build`
- Stage 2: `nginx:alpine` → kopiert `dist/` nach `/usr/share/nginx/html`, kopiert `nginx.conf`

*frontend/nginx.conf – Kernkonfiguration:*

```
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
location / {
    try_files $uri $uri/ /index.html;
}
```

*Dateistruktur für files/-Volume anlegen:*
Alle Unterordner (`Input/Stammdaten`, `Input/Transaktionen`, `Processed/Success`, `Processed/Warning`, `Processed/Error`, `Output/Reports`, `Output/Orders`) müssen mit `.gitkeep`-Dateien im Repo existieren.

**Muster:** Standard Docker Multi-Stage-Pattern

**Testszenarien:**

- Happy path: `docker-compose up --build` startet alle drei Container ohne Fehler. `http://localhost` ist erreichbar (nginx).
- Happy path: `http://localhost/api/v1/health` (wenn Backend-Health-Endpoint implementiert) antwortet mit `200 OK`.
- Edge case: `docker-compose up` ohne `.env`-Datei → Backend startet nicht wegen fehlender DB-Credentials (erwartetes Verhalten, nicht ein Bug).
- Edge case: Backend startet vor DB → `depends_on` mit `condition: service_healthy` oder Retry-Logik via `spring.datasource.hikari.`* absichern.

**Verifikation:** `docker-compose up --build` läuft durch; alle Container sind `healthy`/`running` in `docker ps`.

---

- **Unit 2: Spring Boot Projektkonfiguration & Datenbankschema**

**Ziel:** Vollständige Maven-Konfiguration, `application.yml`, `schema.sql` und Spring-Sicherheitsgrundlagen als Fundament für alle weiteren Units.

**Requirements:** R-D1, R-D2, R_3.1.1, R-A1

**Abhängigkeiten:** Unit 1 (Docker-Infrastruktur)

**Dateien:**

- Modifizieren: `backend/pom.xml` (alle Dependencies eintragen)
- Erstellen: `backend/src/main/resources/application.yml` (ersetzt `application.properties`)
- Löschen: `backend/src/main/resources/application.properties`
- Erstellen: `backend/src/main/resources/schema.sql`
- Erstellen: `backend/src/main/resources/data.sql` (optionale Testdaten)

**Ansatz:**

*pom.xml – vollständige Dependency-Liste:*


| Dependency        | Group/Artifact                          | Version  |
| ----------------- | --------------------------------------- | -------- |
| Spring Web        | `spring-boot-starter-web`               | via BOM  |
| Spring Data JPA   | `spring-boot-starter-data-jpa`          | via BOM  |
| Spring Security   | `spring-boot-starter-security`          | via BOM  |
| Spring Validation | `spring-boot-starter-validation`        | via BOM  |
| Spring Mail       | `spring-boot-starter-mail`              | via BOM  |
| PostgreSQL Driver | `org.postgresql:postgresql`             | via BOM  |
| Lombok            | `org.projectlombok:lombok`              | via BOM  |
| Apache POI        | `org.apache.poi:poi-ooxml`              | `5.2.5`  |
| OpenCSV           | `com.opencsv:opencsv`                   | `5.9`    |
| OpenPDF           | `com.github.librepdf:openpdf`           | `1.3.43` |
| JAXB API          | `jakarta.xml.bind:jakarta.xml.bind-api` | `4.0.2`  |
| JAXB Impl         | `com.sun.xml.bind:jaxb-impl`            | `4.0.5`  |
| Spring Test       | `spring-boot-starter-test`              | via BOM  |


*application.yml – Schlüsselkonfiguration:*

```yaml
spring:
  application.name: lager-management
  datasource:
    url: jdbc:postgresql://${DB_HOST:db}:5432/${DB_NAME:lagerdb}
    username: ${DB_USER:lager}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none          # Schema nur via schema.sql
    show-sql: false
  sql:
    init:
      mode: always            # schema.sql immer ausführen

app:
  security:
    operator-password-hash: ${APP_OPERATOR_PASSWORD_HASH}
  files:
    base-path: ${FILES_BASE_PATH:/app/files}
  reorder:
    consumption-period-days: 30
    cron-expression: "0 0 2 * * *"   # täglich 02:00 Uhr
  import:
    cron-expression: "0 */15 * * * *" # alle 15 Minuten prüfen
  report:
    cron-expression: "0 30 6 * * *"   # täglich 06:30 Uhr

mail:
  enabled: false  # via Umgebungsvariable MAIL_ENABLED aktivieren
```

*schema.sql:* Vollständiges SQL-Schema wie im Abschnitt "Datenbankschema" dieser Datei definiert.

*data.sql (optional):* Mindestens ein Test-Lieferant und ein Test-Artikel für Entwicklungszwecke.

**Testszenarien:**

- Happy path: Backend startet ohne Fehler; DB-Tabellen sind nach dem Start vorhanden (prüfen via `docker exec -it db psql -U lager -d lagerdb -c '\dt'`).
- Error path: Falsches DB-Passwort in `.env` → Backend loggt klar lesbaren Connection-Error und startet nicht.
- Edge case: `schema.sql` wird bei jedem Restart ausgeführt – `CREATE TABLE IF NOT EXISTS` verhindert Datenverlust.

**Verifikation:** Spring-Boot-Startlog zeigt keine Fehler. `GET http://localhost/api/v1/health` (später) gibt `200` zurück.

---

### Phase 2: Domain Model

---

- **Unit 3: JPA Entities & Repositories**

**Ziel:** Vollständiges Java-Domain-Modell, das das Datenbankschema abbildet, plus alle Spring Data JPA Repositories mit benötigten Custom-Queries.

**Requirements:** R_2.1.1, R_2.1.2, R_2.2.1, R-S1, R-S2, R-S3

**Abhängigkeiten:** Unit 2 (schema.sql muss existieren)

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/Artikel.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/Lieferant.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/Bestellung.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/Bestellvorschlag.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/Transaktion.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/entity/ImportLog.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/ArtikelRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/LieferantRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/BestellungRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/BestellvorschlagRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/TransaktionRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/repository/ImportLogRepository.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/enums/ArtikelStatus.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/enums/VorschlagStatus.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/enums/TransaktionTyp.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/domain/enums/TransaktionQuelle.java`
- Test: `backend/src/test/java/com/lagermanagement/space/domain/repository/ArtikelRepositoryTest.java`

**Ansatz:**

Alle Entities nutzen Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Enums statt String-Konstanten für Status-Felder. `@EntityListeners(AuditingEntityListener.class)` für automatische `erstellt_am`/`aktualisiert_am`-Felder (via `@EnableJpaAuditing` in Config-Klasse).

*Wichtige Custom Repository-Methoden:*

```
ArtikelRepository:
  findAllByStatus(ArtikelStatus status)
  findByArtikelnummer(String artikelnummer) → Optional<Artikel>
  existsByArtikelnummer(String artikelnummer)
  findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus, int) → für Reorder-Job

BestellvorschlagRepository:
  findByArtikelIdAndStatusIn(Long artikelId, List<VorschlagStatus>) → prüft offene Vorschläge
  findAllByStatusIn(List<VorschlagStatus> statuses) → Dashboard-Abfrage
  findTopByArtikelIdAndStatusOrderByErstelltAmDesc(Long, VorschlagStatus) → letzter IGNORIERT-Eintrag

BestellungRepository:
  findByBestellnummer(String) → Optional<Bestellung>
  findAllByArtikelIdAndStatus(Long artikelId, String status) → offene Bestellungen für Wareneingang

TransaktionRepository:
  findByArtikelIdAndTypAndDatumBetween(Long, TransaktionTyp, LocalDate, LocalDate) → Verbrauchsanalyse
  findAllByArtikelIdOrderByDatumDesc(Long) → Transaktionshistorie
  findAllByDatumBetween(LocalDate, LocalDate, Pageable) → gefilterte Historie

ImportLogRepository:
  existsByDateiHash(String hash) → Doppelverarbeitungs-Check
```

**Muster zu folgen:**

- Standard JPA-Annotationen (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@Column`)
- Lombok statt manuelle Getter/Setter
- Enums mit `@Enumerated(EnumType.STRING)`

**Testszenarien:**

- Happy path: `ArtikelRepository.save()` speichert Artikel mit Pflichtfeldern erfolgreich.
- Happy path: `findByArtikelnummer()` findet gespeicherten Artikel; gibt `Optional.empty()` zurück wenn nicht vorhanden.
- Edge case: `save()` eines Artikels mit doppelter `artikelnummer` → `DataIntegrityViolationException` (DB-Constraint).
- Happy path: `findAllByStatusAndBestellpunktGreaterThan(AKTIV, 0)` gibt nur AKTIVE Artikel mit gesetztem Bestellpunkt zurück.
- Integration: `BestellvorschlagRepository.findByArtikelIdAndStatusIn()` gibt korrekt gefilterte Vorschläge zurück.

**Verifikation:** `@DataJpaTest`-Tests laufen grün. Schema-Validierung: Alle Entities mappen korrekt auf DB-Tabellen.

---

### Phase 3: Sicherheit & Querschnitt

---

- **Unit 4: Spring Security, Auth-Endpoint & globale Fehlerbehandlung**

**Ziel:** Absicherung aller API-Endpunkte via HTTP Basic Auth mit httpOnly-Session-Cookie. Zentrales, konsistentes Fehlerformat für alle REST-Fehler.

**Requirements:** R-A1, R-A2, R-A3, R_3.5.1, R_3.5.4

**Abhängigkeiten:** Unit 3

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/config/SecurityConfig.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/config/JpaAuditingConfig.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/AuthController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/handler/GlobalExceptionHandler.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/dto/ApiErrorDto.java`
- Test: `backend/src/test/java/com/lagermanagement/space/web/controller/AuthControllerTest.java`

**Ansatz:**

*SecurityConfig:*

- `UserDetailsService`: Einzelner User `operator` mit BCrypt-Hash aus `${app.security.operator-password-hash}`.
- Alle Requests außer `/api/v1/auth/login` (POST) benötigen Authentifizierung.
- Session-Management: `SessionCreationPolicy.IF_REQUIRED` (Standard). Spring Security erstellt nach erfolgreichem Login ein `JSESSIONID`-Cookie mit `httpOnly=true`.
- CSRF: Für REST-SPA-Pattern deaktivieren (kein Form-Submit). Sicherheitsrisiko minimal da nur lokales Netz + `httpOnly`-Cookie (kein JS-Zugriff).
- Login-Erfolg: `200 OK` mit User-Info statt Redirect.
- Login-Fehler: `401 Unauthorized` mit JSON-Body statt Default-HTML.
- Unauthorized-Handler: `401` als JSON, kein Redirect auf Login-Seite.

*AuthController:*

- `POST /api/v1/auth/login` → Authentifizierung via Spring Security, gibt `{ "username": "operator" }` zurück.
- `POST /api/v1/auth/logout` → `SecurityContextHolder.clearContext()`, Session invalidieren.
- `GET /api/v1/auth/me` → Gibt aktuellen Username aus Security-Kontext zurück (nützlich für Frontend-Initialisierung).

*Benutzer-ID für Audit Trail:*

- `SecurityContextHolder.getContext().getAuthentication().getName()` liefert `"operator"`.
- Dieser Wert wird in `Bestellung.erstellt_von` und `Transaktion.benutzer_id` gespeichert.
- Helper-Methode `SecurityUtils.getCurrentUsername()` als statische Util-Klasse.

*GlobalExceptionHandler (`@ControllerAdvice`):*

```
ApiErrorDto { status, message, timestamp, details }

Behandelt:
  - MethodArgumentNotValidException → 400 mit Feldfehlerliste
  - EntityNotFoundException (custom) → 404
  - DataIntegrityViolationException → 409 (Konflikt, z.B. doppelte Artikelnummer)
  - BusinessException (custom) → 422 (z.B. kein offene Bestellung für Wareneingang)
  - Exception (fallback) → 500
```

*Zwei Custom Exceptions erstellen:*

- `EntityNotFoundException extends RuntimeException`
- `BusinessException extends RuntimeException`

**Testszenarien:**

- Happy path: `POST /api/v1/auth/login` mit korrekten Credentials → `200`, Cookie `JSESSIONID` im Response-Header.
- Error path: Login mit falschem Passwort → `401` mit JSON-Body (nicht HTML).
- Error path: Zugriff auf `/api/v1/artikel` ohne Session → `401` mit JSON (nicht Redirect).
- Happy path: Nach Login ist `/api/v1/auth/me` erreichbar und gibt `"operator"` zurück.
- Happy path: Nach Logout ist `/api/v1/artikel` nicht mehr erreichbar → `401`.

**Verifikation:** Alle Security-Tests grün. Manueller Test: Login über Browser-DevTools, JSESSIONID-Cookie ist `httpOnly`.

---

### Phase 4: Batch-Verarbeitung

---

- **Unit 5: Datei-Import-Services (Stammdaten & Transaktionen)**

**Ziel:** Robuster, zeitgesteuerter Dateiimport für alle 4 Datei-Typen. Skip bei Zeilenfehlern, Verschieben nach Success/Warning/Error. Kein Doppelimport.

**Requirements:** R-B1, R-B2, R-B3, R-I1, R-I2, R-I3, R_2.1.3, R_2.2.2, R-L4

**Abhängigkeiten:** Unit 3 (Repositories), Unit 2 (application.yml mit `app.files.base-path`)

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/FileService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/batch/StammdatenImportService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/batch/TransaktionenImportService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/batch/ImportScheduler.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/batch/FileParser.java` (Util-Klasse: CSV + Excel-Parsing)
- Test: `backend/src/test/java/com/lagermanagement/space/service/batch/StammdatenImportServiceTest.java`
- Test: `backend/src/test/java/com/lagermanagement/space/service/batch/TransaktionenImportServiceTest.java`

**Ansatz:**

*FileService (Kernoperationen):*

- `scanDirectory(Path dir, String pattern)` → Liste von Path-Objekten
- `computeHash(Path file)` → SHA-256 als Hex-String
- `moveFile(Path source, Path targetDir)` → verschiebt, erstellt Zielordner (inkl. `YYYY-MM-DD/`) bei Bedarf
- `resolveOutputPath(String subPath)` → kombiniert `app.files.base-path` + `subPath`

*Import-Ablauf (für jeden Service gleich):*

```
1. Scanne Input-Verzeichnis nach *.csv und *.xlsx
2. Für jede Datei:
   a. Berechne SHA-256-Hash
   b. Prüfe ImportLogRepository.existsByDateiHash(hash)
      → wenn true: überspringe (LOG INFO), weiter mit nächster Datei
   c. Parse Datei (FileParser.parse())
   d. Für jede Zeile: Validiere und persistiere
      → bei Fehler: überspringe Zeile, zähle fehlerhafteZeilen++, logge Fehler
   e. Bewerte Ergebnis:
      → 0 Zeilen erfolgreich: Status ERROR → Processed/Error/YYYY-MM-DD/
      → 0 < fehlerhafteZeilen: Status WARNING → Processed/Warning/YYYY-MM-DD/
      → Alle OK: Status SUCCESS → Processed/Success/YYYY-MM-DD/
   f. Speichere ImportLog-Eintrag mit Hash, Status, Zählern
```

*StammdatenImportService – Besonderheiten:*

- Erkennt Datei-Typ anhand des Dateinamens-Präfixes (`artikel_`* vs. `lieferanten_*`).
- **Lieferanten zuerst** verarbeiten (FK-Abhängigkeit: Artikel referenziert Lieferant).
- Bei `artikelnummer` bereits vorhanden → UPDATE (Upsert-Logik), kein Fehler.
- Bei `lieferant_id` in Artikel-Zeile nicht gefunden → Zeile überspringen, Fehler loggen.

*TransaktionenImportService – Besonderheiten:*

- Erkennt Datei-Typ anhand des Dateinamens-Präfixes (`eingang_`* vs. `ausgang_*`).
- Bestandsaktualisierung im **gleichen `@Transactional`-Block** wie das Speichern der Transaktion.
- Für Eingang: `artikel.aktuellerBestand += menge`. Prüft danach R-L4: Wenn `bestellung_referenz` vorhanden und eine OFFENE Bestellung mit dieser Nummer existiert → Bestellung auf `GELIEFERT` setzen, Bestellvorschlag auf `GELIEFERT` setzen.
- Für Ausgang: `artikel.aktuellerBestand -= menge`. Prüft Bestand >= 0 (kein Negativbestand ohne explizite Korrektur).

*FileParser:*

- Erkennt Format (CSV vs. Excel) anhand Dateiendung.
- Gibt `List<Map<String, String>>` zurück (Spaltenkopf → Wert, normalisiert auf lowercase).
- Validiert, ob alle Pflicht-Spalten vorhanden sind (sonst: Datei als ERROR klassifizieren).

**Testszenarien:**

- Happy path: Artikel-CSV mit 5 gültigen Zeilen → 5 Artikel in DB, Datei in `Processed/Success/`.
- Happy path: Lieferanten werden vor Artikeln verarbeitet.
- Edge case (Doppelverarbeitung): Gleiche Datei zweimal in `Input/` legen → zweiter Lauf überspringt die Datei (Hash bereits in `import_log`).
- Error path (Teilfehler): CSV mit 5 Zeilen, Zeile 3 hat ungültige `lieferant_id` → 4 Zeilen importiert, Datei in `Processed/Warning/`, `import_log` zeigt `zeilen_fehlerhaft=1`.
- Error path (Komplett-Fehler): CSV ohne Pflicht-Spalten → Datei in `Processed/Error/`, keine DB-Einträge.
- Integration (R-L4): Eingang-CSV mit `bestellung_referenz=BEST-2026-042` → Bestellvorschlag und Bestellung werden automatisch auf `GELIEFERT` gesetzt.
- Edge case: Negativbestand bei Ausgang → Zeile überspringen, Warnung loggen.

**Verifikation:** Alle Tests grün. Manueller Integrationstest: Test-CSV in `Input/Stammdaten/` legen, nach nächstem Scheduler-Lauf in DB prüfen.

---

- **Unit 6: Reorder-Analyse-Job & Täglicher Bestandsbericht**

**Ziel:** Nächtlicher Reorder-Job generiert Bestellvorschläge. Täglicher Job erstellt Bestandsbericht-PDF.

**Requirements:** R_2.3.1, R_2.3.2, R_2.3.3, R-L3, R-P4, R-P5

**Abhängigkeiten:** Unit 5 (Transaktionsdaten für Verbrauchsanalyse)

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/ReorderAnalysisService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/BerichtService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/batch/ReorderScheduler.java`
- Test: `backend/src/test/java/com/lagermanagement/space/service/ReorderAnalysisServiceTest.java`

**Ansatz:**

*ReorderAnalysisService.runAnalysis():*

```
Für jeden aktiven Artikel (status=AKTIV, bestellpunkt > 0):
  1. Berechne Tagesverbrauch:
     → Lade alle AUSGANG-Transaktionen der letzten N Tage (N = consumption-period-days)
     → tagesverbrauch = sum(menge) / N
  
  2. Prüfe, ob Bestellvorschlag nötig:
     → aktueller_bestand <= bestellpunkt?
  
  3. Prüfe Duplikat/Ignoriert (IGNORIERT-Logik):
     → Existiert ein Vorschlag mit status IN (VORSCHLAG, BESTELLT)? → Überspringen
     → Existiert ein Vorschlag mit status = IGNORIERT?
       → UND aktueller_bestand > sicherheitsbestand? → Überspringen
       → UND aktueller_bestand <= sicherheitsbestand? → Trotzdem neuen Vorschlag erstellen (kritisch!)
  
  4. Wenn Vorschlag nötig und keine Sperr-Bedingung:
     → Erstelle Bestellvorschlag (status=VORSCHLAG)
     → vorgeschlagene_menge = artikel.standard_bestellmenge
```

*BerichtService.generateDailyReport():*

- Lädt alle AKTIVEN Artikel mit Bestand.
- Erstellt PDF via OpenPDF mit Tabelle: Artikelnummer, Bezeichnung, Einheit, Bestand, Bestellpunkt, Sicherheitsbestand, Status (Normal/Kritisch).
- Status "Kritisch" wenn `aktueller_bestand <= sicherheitsbestand`.
- Speichert PDF in `Output/Reports/Bestandsbericht_YYYY-MM-DD.pdf`.

*ReorderScheduler:*

```java
@Scheduled(cron = "${app.reorder.cron-expression}")
public void scheduledReorder() { reorderAnalysisService.runAnalysis(); }

@Scheduled(cron = "${app.report.cron-expression}")
public void scheduledReport() { berichtService.generateDailyReport(); }
```

*Manueller Trigger (für R-P4):*

- `ReorderAnalysisService.runAnalysis()` ist synchron aufrufbar → REST-Controller ruft direkt auf.
- Kein separates Thread-Handling nötig für diesen Scope.

**Testszenarien:**

- Happy path: Artikel mit `bestand=5, bestellpunkt=10` → Vorschlag wird erstellt.
- Happy path: Artikel mit `bestand=15, bestellpunkt=10` → kein Vorschlag.
- Edge case (Duplikat): Artikel hat bereits `status=VORSCHLAG` → kein zweiter Vorschlag.
- Edge case (IGNORIERT, normal): Artikel mit IGNORIERT-Vorschlag, `bestand=15 > sicherheitsbestand=10` → kein neuer Vorschlag.
- Edge case (IGNORIERT, kritisch): Artikel mit IGNORIERT-Vorschlag, `bestand=3 <= sicherheitsbestand=10` → neuer Vorschlag trotzdem erstellt.
- Happy path: Tagesverbrauch korrekt berechnet (sum/N über konfigurierte Periode).
- Integration: Bestandsbericht-PDF wird im korrekten Pfad gespeichert.

**Verifikation:** Unit-Tests für Reorder-Logik grün. Integrations-Trigger via REST zeigt Ergebnis in DB.

---

### Phase 5: REST API

---

- **Unit 7: REST API – Dashboard, Artikel, Lieferanten**

**Ziel:** Vollständige REST-Endpunkte für die Kernfunktionen: Dashboard-KPIs, Artikelverwaltung (CRUD), Lieferantenverwaltung (Read + Update).

**Requirements:** R-P1, R-P2, R-P3, R-N1, R-N2, R-N3, R-V1, R-V2, R_2.4.1, R_2.4.2

**Abhängigkeiten:** Unit 4 (Security), Unit 3 (Repositories)

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/DashboardController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/ArtikelController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/LieferantController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/ArtikelService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/LieferantService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/dto/` (alle DTOs)
- Test: `backend/src/test/java/com/lagermanagement/space/web/controller/ArtikelControllerTest.java`

**API-Endpunkte:**

```
Dashboard:
  GET  /api/v1/dashboard/kpis
       → { offeneVorschlaege: int, kritischeVorschlaege: int, letzterReorderLauf: ISO-Timestamp }
  GET  /api/v1/dashboard/vorschlaege
       → List<BestellvorschlagDto> (status IN VORSCHLAG, BESTELLT)
  POST /api/v1/reorder/trigger
       → { message: "Analyse abgeschlossen", neueVorschlaege: int }

Artikel:
  GET  /api/v1/artikel                    → List<ArtikelDto> (alle aktiven + inaktiven)
  GET  /api/v1/artikel/{id}               → ArtikelDto
  POST /api/v1/artikel                    → 201 Created, ArtikelDto
  PUT  /api/v1/artikel/{id}               → 200 OK, ArtikelDto
  PUT  /api/v1/artikel/{id}/deaktivieren  → 200 OK (Status → INAKTIV)
  GET  /api/v1/artikel/{id}/bestand       → { aktueller_bestand: int }

Lieferanten:
  GET  /api/v1/lieferanten                → List<LieferantDto>
  GET  /api/v1/lieferanten/{id}           → LieferantDto
  PUT  /api/v1/lieferanten/{id}           → 200 OK, LieferantDto
```

*Wichtige DTO-Klassen:*

```
ArtikelDto: id, artikelnummer, bezeichnung, mengeneinheit, warengruppe,
            lieferantId, lieferantName, aktuellerBestand, sicherheitsbestand,
            bestellpunkt, standardBestellmenge, einkaufspreis, status

BestellvorschlagDto: id, artikelId, artikelnummer, artikelBezeichnung,
                     lieferantName, aktuellerBestand, bestellpunkt, sicherheitsbestand,
                     vorgeschlageneMenge, status, erstelltAm,
                     istKritisch (= aktuellerBestand <= sicherheitsbestand)

KpiDto: offeneVorschlaege, kritischeVorschlaege, letzterReorderLauf

LieferantDto: id, lieferantId, name, kontaktEmail, kontaktTelefon, leadTimeTage
```

*Validierung (R-N2):*

- `POST /api/v1/artikel`: `artikelnummer` auf Eindeutigkeit prüfen → wenn vorhanden: `409 Conflict`.
- `@Valid`-Annotationen auf Request-Body-DTOs.

**Testszenarien:**

- Happy path: `GET /api/v1/artikel` gibt leere Liste zurück, wenn keine Artikel vorhanden.
- Happy path: `POST /api/v1/artikel` mit gültigen Daten → 201, Artikel in DB.
- Error path: `POST /api/v1/artikel` mit doppelter `artikelnummer` → 409.
- Error path: `POST /api/v1/artikel` ohne Pflichtfelder → 400 mit Feldfehlern.
- Happy path: `PUT /api/v1/artikel/{id}/deaktivieren` → Artikel-Status wird `INAKTIV`.
- Error path: `GET /api/v1/artikel/{unbekannte-id}` → 404.
- Happy path: `GET /api/v1/dashboard/kpis` gibt korrekte Zählwerte zurück.
- Happy path: `POST /api/v1/reorder/trigger` startet Job synchron, gibt Ergebnis zurück.

**Verifikation:** `@WebMvcTest`-Tests mit gemockten Services grün. Manuelle API-Tests via Postman/HTTPie.

---

- **Unit 8: REST API – Lagerbewegungen, Bestellvorschlag-Lifecycle & Output-Generierung**

**Ziel:** Manuelle Warenein-/ausgänge, vollständiger Bestellvorschlag-Workflow (Schnellbestellung, Bearbeiten, Ignorieren), PDF- und XML-Generierung.

**Requirements:** R-M1, R-M2, R-M3, R-M4, R-M5, R-L1, R-L2, R-L3, R-L5, R-O1, R-O2, R-O3, R-T1, R-T2, R_2.4.4, R_3.5.4

**Abhängigkeiten:** Unit 7, Unit 3

**Dateien:**

- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/BewegungController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/BestellungController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/controller/TransaktionController.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/InventoryService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/OrderService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/PdfGeneratorService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/XmlGeneratorService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/service/MailAlertService.java`
- Erstellen: `backend/src/main/java/com/lagermanagement/space/web/dto/xml/BestellungXml.java` (JAXB-annotiert)
- Test: `backend/src/test/java/com/lagermanagement/space/service/OrderServiceTest.java`
- Test: `backend/src/test/java/com/lagermanagement/space/service/InventoryServiceTest.java`

**API-Endpunkte:**

```
Manuelle Lagerbewegungen:
  POST /api/v1/bewegungen/eingang
       Request: { artikelId, menge, datum, bestellungId? }
       → Prüft: offene Bestellung für Artikel existiert? (R-M1)
       → Bucht Bestand +menge
       → Fragt nach Bestellungsabschluss (Response: { transaktionId, bestellungKannGeschlossenWerden: bool, bestellungId })
       Response: 201

  POST /api/v1/bewegungen/eingang/{transaktionId}/bestellung-abschliessen
       → Setzt Bestellung auf GELIEFERT (R-M2, R-L5)
       Response: 200

  POST /api/v1/bewegungen/ausgang
       Request: { artikelId, menge, datum, buchungstyp, grund? }
       → Bucht Bestand -menge, speichert Transaktion mit quelle=MANUELL
       Response: 201

Bestellvorschlag-Lifecycle:
  POST /api/v1/bestellungen/schnell/{vorschlagId}
       → Erstellt Bestellung mit Standardwerten, generiert PDF+XML
       Response: 201, { bestellungId, bestellnummer, pdfPfad, xmlPfad }

  POST /api/v1/bestellungen/bearbeitet/{vorschlagId}
       Request: { bestellmenge, lieferantId, gewuensteresLieferdatum, notiz }
       → Erstellt Bestellung mit angepassten Werten, generiert PDF+XML
       Response: 201, { bestellungId, bestellnummer, pdfPfad, xmlPfad }

  PUT  /api/v1/bestellvorschlaege/{vorschlagId}/ignorieren
       → Status → IGNORIERT
       Response: 200

Transaktionshistorie:
  GET  /api/v1/transaktionen
       Query-Params: artikelId?, von?, bis?, seite=0, groesse=50
       → PagedResult<TransaktionDto>

Bestellhistorie:
  GET  /api/v1/bestellungen
       → List<BestellungDto> (alle, absteigende Reihenfolge)
  GET  /api/v1/bestellungen/{id}
       → BestellungDto
```

*InventoryService.bucheEingang():*

```
1. Lade Artikel → EntityNotFoundException wenn nicht vorhanden
2. Prüfe offene Bestellung: BestellungRepository.findAllByArtikelIdAndStatus(artikelId, "OFFEN")
   → wenn leer: throw BusinessException("Keine offene Bestellung für diesen Artikel")
3. artikel.aktuellerBestand += menge (innerhalb @Transactional)
4. Speichere Transaktion (quelle=MANUELL, typ=EINGANG, benutzer_id=currentUser)
5. Rückgabe: { transaktionId, kann Bestellung geschlossen werden }
```

*OrderService.erstelleBestellung():*

```
1. Generiere Bestellnummer: "BEST-{YYYY}-{NNN}" (sequenziell pro Jahr)
2. Setze Vorschlag-Status → BESTELLT, verknüpfe bestellung_id
3. Speichere Bestellung (erstellt_von = aktueller Benutzer → Audit Trail)
4. Rufe PdfGeneratorService.generate(bestellung) → speichert PDF
5. Rufe XmlGeneratorService.generate(bestellung) → speichert XML
6. Beide Dateien in Output/Orders/Bestellung_{nr}_{datum}.{pdf|xml}
```

*PDF-Inhalt (R-O3):* Bestellnummer, Datum, Lieferantname + Kontakt, Artikel (Nr., Bezeichnung, Menge, Einheit, Einkaufspreis, Teilbetrag), Gesamtbetrag, Lieferdatum, Notiz, Erstellt-von, Firmenlogo-Platzhalter (konfigurierbarer Pfad).

*XML-Struktur (R-O1):* JAXB-Klasse `BestellungXml` mit `@XmlRootElement("bestellung")`. Felder entsprechen PDF-Inhalt.

**Testszenarien:**

- Happy path: Schnellbestellung für Vorschlag → Bestellung in DB, PDF + XML auf Dateisystem, Status `BESTELLT`.
- Error path: Wareneingang ohne offene Bestellung → `422` mit BusinessException-Meldung.
- Happy path: Warenausgang `Verbrauch intern` → Bestand sinkt, Transaktion gespeichert mit `quelle=MANUELL`.
- Edge case: Warenausgang über verfügbaren Bestand → `422` (Negativbestand nicht erlaubt).
- Happy path: Ignorieren eines Vorschlags → Status `IGNORIERT`, erscheint nicht mehr in Dashboard-Abfrage.
- Integration (Audit Trail): `Bestellung.erstellt_von` enthält `"operator"` nach Login.
- Happy path: `GET /api/v1/transaktionen?artikelId=1&von=2026-04-01&bis=2026-04-30` gibt gefilterte Transaktionen zurück.

**Verifikation:** Service-Tests grün. Integrations-Walkthrough: Vorschlag erstellen → Schnellbestellen → PDF prüfen → Wareneingang → Bestellung schließen.

---

### Phase 6: Vue.js Frontend

---

- **Unit 9: Vue.js Grundstruktur, Routing & Auth-Flow**

**Ziel:** Voll funktionsfähiges Vue.js 3 Projekt mit PrimeVue, Axios-Interceptors, Vue Router mit Auth-Guard und Login-Seite.

**Requirements:** R-A3, R-P1, R-P2

**Abhängigkeiten:** Unit 4 (Auth-API fertig)

**Dateien:**

- Erstellen: `frontend/package.json`
- Erstellen: `frontend/vite.config.js`
- Erstellen: `frontend/index.html`
- Erstellen: `frontend/src/main.js`
- Erstellen: `frontend/src/App.vue`
- Erstellen: `frontend/src/router/index.js`
- Erstellen: `frontend/src/api/axios.js`
- Erstellen: `frontend/src/api/services/authService.js`
- Erstellen: `frontend/src/stores/authStore.js` (Pinia oder reaktives `ref`)
- Erstellen: `frontend/src/views/LoginView.vue`
- Erstellen: `frontend/src/components/AppHeader.vue` (Navigation/Logout)

**Ansatz:**

*Abhängigkeiten (package.json):*

- `vue@3.x`, `vue-router@4.x`, `pinia@2.x` (oder reaktives State ohne Pinia für Einfachheit)
- `primevue@4.x`, `primeicons`, `@primevue/themes` (für Aura/Lara Theme)
- `axios@1.x`

*axios.js – Zentralkonfiguration:*

- Basis-URL: `/api/v1` (relativ → Nginx-Proxy übernimmt)
- `withCredentials: true` → sendet Session-Cookie automatisch mit
- Response-Interceptor: Bei `401` → zu Login-Seite weiterleiten
- Request-Interceptor: Kein manuelles Token-Handling (Cookie übernimmt alles)

*router/index.js:*

- Route `/login` → `LoginView.vue`
- Alle anderen Routen: Navigation Guard prüft via `GET /api/v1/auth/me` ob Session aktiv
- `meta: { requiresAuth: true }` für alle geschützten Routen

*LoginView.vue:*

- PrimeVue `InputText` + `Password` + `Button`
- Ruft `POST /api/v1/auth/login` auf
- Bei Erfolg: weiterleiten zu `/dashboard`
- Bei Fehler: Inline-Fehlermeldung (PrimeVue `Message`)

**Testszenarien:**

- Happy path: Login-Formular mit korrekten Daten → Weiterleitung zu `/dashboard`.
- Error path: Login mit falschen Daten → Fehlermeldung unter Formular.
- Edge case: Direkter Aufruf von `/dashboard` ohne Session → Weiterleitung zu `/login`.
- Happy path: Nach Logout → Session ungültig → nächster API-Aufruf → Weiterleitung zu `/login`.

**Verifikation:** Login-Flow im Browser funktioniert. Netzwerk-Tab zeigt `JSESSIONID`-Cookie bei API-Aufrufen.

---

- **Unit 10: Vue.js Dashboard, Alle Kernseiten & Lagerbewegungs-Formulare**

**Ziel:** Vollständige, interaktive Benutzeroberfläche: Dashboard mit KPIs und Bestellvorschlag-Workflow, alle Seiten (Artikel, Lieferanten, Transaktionshistorie, Bestellhistorie), Formulare für Lagerbewegungen.

**Requirements:** R-P1 bis R-P5, R-L1 bis R-L5, R-M1 bis R-M5, R-N1 bis R-N4, R-V1, R-V2, R-T1, R-T2

**Abhängigkeiten:** Unit 9, Unit 7, Unit 8 (alle API-Endpunkte fertig)

**Dateien:**

- Erstellen: `frontend/src/views/DashboardView.vue`
- Erstellen: `frontend/src/views/ArtikelView.vue`
- Erstellen: `frontend/src/views/LieferantenView.vue`
- Erstellen: `frontend/src/views/TransaktionshistorieView.vue`
- Erstellen: `frontend/src/views/BestellhistorieView.vue`
- Erstellen: `frontend/src/views/LagerbewegungView.vue`
- Erstellen: `frontend/src/components/KpiSummary.vue`
- Erstellen: `frontend/src/components/BestellvorschlagTabelle.vue`
- Erstellen: `frontend/src/components/BestellModal.vue`
- Erstellen: `frontend/src/components/ArtikelFormular.vue`
- Erstellen: `frontend/src/components/WareineingangFormular.vue`
- Erstellen: `frontend/src/components/WarenausgangFormular.vue`
- Erstellen: `frontend/src/api/services/` (alle Service-Dateien)

**Dashboard-Layout (R-P1 bis R-P5):**

```
┌──────────────────────────────────────────────────────────┐
│  KPI-Summary                                              │
│  [ X offene Vorschläge ]  [ Y kritisch ]  [ Letzter Lauf ]│
│  [ Button: "Analyse jetzt ausführen" ]                    │
├──────────────────────────────────────────────────────────┤
│  Quick-Link-Kacheln (PrimeVue Card):                      │
│  [Lagerbewegung] [Artikel] [Lieferanten]                  │
│  [Transaktionen] [Bestellhistorie]                        │
├──────────────────────────────────────────────────────────┤
│  Bestellvorschläge-Tabelle (PrimeVue DataTable)           │
│  Farb-Logik:                                              │
│    - Rot: status=VORSCHLAG + bestand <= sicherheitsbestand│
│    - Gelb: status=VORSCHLAG + bestand > sicherheitsbestand│
│    - Grau: status=BESTELLT (= "In Lieferung")             │
│  Aktionen pro Zeile: [Schnellbestellen] [Bearbeiten] [Ignorieren] │
└──────────────────────────────────────────────────────────┘
```

*BestellModal.vue:*

- Öffnet sich bei "Bearbeiten" mit vorausgefüllten Feldern.
- Felder: Bestellmenge (editierbar), Lieferant (Dropdown), gewünschtes Lieferdatum (Kalender-Picker), Notiz (Textarea).
- Buttons: "Bestellung freigeben" / "Abbrechen".

*WareineingangFormular.vue:*

- Dropdown: Artikel auswählen.
- Menge-Feld.
- Datum-Picker.
- Validierung: Artikel muss offene Bestellung haben (Backend gibt `422` → Frontend zeigt Fehlermeldung).
- Nach erfolgreichem Eingang: Bestätigungsdialog "Soll die Bestellung als geliefert markiert werden?" (PrimeVue `ConfirmDialog`).

*ArtikelFormular.vue (für Anlage + Bearbeitung):*

- Alle Felder laut R-N1: Artikelnummer (bei Bearbeitung read-only), Bezeichnung, Einheit, Warengruppe, Lieferant (Dropdown), Sicherheitsbestand, Bestellpunkt, Standardbestellmenge, Einkaufspreis.
- Inline-Validierung.
- "Speichern" / "Abbrechen" Buttons.

*TransaktionshistorieView.vue:*

- PrimeVue DataTable mit serverseitiger Paginierung.
- Filter: Artikel-Dropdown, Datum-von/bis (Kalender-Picker).
- Spalten: Datum, Artikel, Typ, Menge, Buchungstyp, Quelle, Benutzer.

**Testszenarien:**

- Happy path: Dashboard lädt KPIs und Vorschlagstabelle korrekt.
- Happy path: Schnellbestell-Klick → Bestätigungsdialog → Vorschlag wechselt zu grau ("In Lieferung").
- Happy path: Ignorieren → Bestätigungsdialog → Vorschlag verschwindet aus Tabelle.
- Happy path: Neuen Artikel anlegen via Formular → erscheint in Artikelliste.
- Error path: Doppelte Artikelnummer → Inline-Fehlermeldung "Artikelnummer bereits vorhanden".
- Happy path: Wareneingang buchen → Bestätigungsdialog → Bestand im Artikel-Detail aktualisiert.
- Error path: Wareneingang ohne offene Bestellung → klare Fehlermeldung.
- Happy path: Transaktionshistorie nach Artikel filtern → nur Transaktionen dieses Artikels.
- Happy path: Reorder-Trigger-Button → Spinner während Request → Dashboard-Tabelle aktualisiert sich.

**Verifikation:** Vollständiger E2E-Walkthrough: Login → Dashboard → Vorschlag bearbeiten → Bestellung freigeben → Wareneingang buchen → Transaktionshistorie prüfen.

---

## System-Wide Impact

- **Transaktionssicherheit:** `InventoryService.bucheEingang/Ausgang()` und `OrderService.erstelleBestellung()` laufen in `@Transactional`. Bestandsänderung und Transaktionsspeicherung sind atomar.
- **Audit Trail:** `Bestellung.erstellt_von` und `Transaktion.benutzer_id` werden immer über `SecurityUtils.getCurrentUsername()` gefüllt. Der Service-Layer ist dafür verantwortlich, nicht der Controller.
- **Bestandsinkonsistenz-Risiko:** Der `aktueller_bestand` in `artikel` ist denormalisiert. Alle Bestandsänderungen (Batch + Manuell) müssen **ausschließlich** über `InventoryService` laufen – direkte Repository-Updates auf `Artikel.aktuellerBestand` sind verboten.
- **Reorder-Job nach manuellem Ausgang:** Manuelle Warenausgänge (`quelle=MANUELL`) fließen gleichwertig in die Verbrauchsanalyse ein (R-M5), da die `TransaktionRepository.findByArtikelIdAndTypAndDatumBetween()`-Abfrage keine Quellen-Einschränkung hat.
- **Unveränderliche Invarianten:** `bestellungen`-Einträge werden nach Erstellung nicht mehr geändert (immutable). Status-Übergänge von Bestellvorschlägen (`VORSCHLAG → BESTELLT → GELIEFERT/IGNORIERT`) sind nur vorwärts erlaubt.
- **Keine API-Änderungen:** `Lieferant.lieferant_id` (Batch-ID) und `Artikel.artikelnummer` sind unveränderlich nach Anlage (nicht editierbar via PUT).

---

## Risiken & Abhängigkeiten


| Risiko                                                                                                                       | Mitigation                                                                                          |
| ---------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `schema.sql` mit `CREATE TABLE IF NOT EXISTS` wird bei jedem Start ausgeführt → Datenmigration bei Schema-Änderungen manuell | Für Portfolio-Scope akzeptiert. Scope-Erweiterung: Flyway einführen.                                |
| Apache POI lädt große Excel-Dateien vollständig in den Speicher                                                              | Für Projektvolumen unkritisch. Bei >10.000 Zeilen: SXSSF-API nutzen.                                |
| Synchroner Reorder-Trigger blockiert HTTP-Request für mehrere Sekunden                                                       | Frontend zeigt Lade-Spinner. Für Portfolio-Scope akzeptiert.                                        |
| Bestand kann bei gleichzeitigen Batch-Imports falsch berechnet werden                                                        | `@Transactional` + DB-Locking auf Artikel-Zeile via `@Lock(PESSIMISTIC_WRITE)` in InventoryService. |
| CSRF-Schutz deaktiviert                                                                                                      | Nur lokales/internes Netz, kein öffentlicher Betrieb. Explizit in Scope Boundaries definiert.       |


---

## Dokumentations- & Betriebshinweise

- `**.env`-Template:** Eine Datei `.env.example` (mit Platzhaltern, ohne echte Werte) wird ins Repo eingecheckt. `.env` ist in `.gitignore`.
- **Start-Anleitung im README:** `docker-compose up --build`, `files/`-Ordnerstruktur anlegen, Login-Credentials.
- **Import-Schemas:** Die Tabellen aus diesem Dokument (Abschnitt "Datei-Import-Schemas") in die Benutzerdokumentation übernehmen.
- **SMTP-Konfiguration:** `mail.enabled=true` + SMTP-Vars in `.env` setzen, wenn E-Mail-Alerts gewünscht.

---

## Quellen & Referenzen

- **Origin-Dokument:** `[docs/brainstorms/2026-04-14-lager-optimierung-requirements.md](../brainstorms/2026-04-14-lager-optimierung-requirements.md)`
- **Projektdefinition:** `[zdocs/projekt_definition.md](../../zdocs/projekt_definition.md)`
- Existierender Code: `src/main/java/com/lagermanagement/space/SpaceApplication.java`
- Spring Boot 3.x Docs: [https://docs.spring.io/spring-boot/docs/3.x/reference/html/](https://docs.spring.io/spring-boot/docs/3.x/reference/html/)
- PrimeVue 4.x Docs: [https://primevue.org/](https://primevue.org/)
- OpenPDF: [https://github.com/LibrePDF/OpenPDF](https://github.com/LibrePDF/OpenPDF)
- JAXB Jakarta: [https://eclipse-ee4j.github.io/jaxb-ri/](https://eclipse-ee4j.github.io/jaxb-ri/)

