# Testergebnis: Infrastruktur & Projektkonfiguration (Tickets 001–005)

**Datum**: 2026-04-20  
**Tickets**: `docs/todo/001` bis `docs/todo/005`  
**Tester**: kev-tester  
**Gesamt-Status**: ⚠️ GELB – Ticket 001 blockiert (Frontend-App nicht vorhanden), Ticket 004 WARN behoben

---

## Getestete Komponenten

| Datei | Testmethode |
|---|---|
| `docker-compose.yml` | `docker compose config` + `docker compose up` (DB + Backend) |
| `backend/Dockerfile` | `docker compose build backend` + Startlogs |
| `frontend/Dockerfile` | `docker compose build` – FEHLER-002 |
| `frontend/nginx.conf` | Strukturprüfung |
| `files/` Verzeichnisstruktur | Filesystem-Check |
| `backend/pom.xml` | `mvn package -DskipTests` |
| `backend/src/main/resources/application.yml` | Laufzeitprüfung (Docker + Fail-Fast-Test) |
| `backend/src/main/resources/schema.sql` | Vollständigkeits- und Konsistenz-Prüfung |

---

## Testergebnisse pro Ticket

### Ticket 001 – Docker Compose, Dockerfiles & Nginx ⚠️ BLOCKIERT

| Prüfung | Ergebnis |
|---|---|
| `docker compose config` Exit-Code 0 | ✅ |
| Nur Port 80 nach außen | ✅ |
| Port 5432 (DB) nicht nach außen erreichbar | ✅ – `docker port` gibt nichts zurück |
| Port 8080 (Backend) nicht nach außen erreichbar | ✅ – `8080/tcp` nur intern |
| Backend Dockerfile baut erfolgreich (`docker compose build backend`) | ✅ – BUILD SUCCESS |
| Backend-Container startet, Spring Boot läuft, HikariCP verbindet | ✅ – `Started SpaceApplication in 18.776 seconds` |
| Frontend Dockerfile baut erfolgreich | ❌ **FEHLER-002** – kein Vue-Projekt vorhanden |
| BCrypt-Hash korrekt an Container übergeben | ✅ (FEHLER-001 bereits behoben) |
| `.env.example` vorhanden | ✅ |
| `.env` in `.gitignore` | ✅ |
| `docker-compose up --build` vollständig (alle 3 Services) | ❌ Blockiert durch FEHLER-002 |

### Ticket 002 – files/-Verzeichnisstruktur ✅

Alle 7 Unterordner mit `.gitkeep` vorhanden. `.gitignore` korrekt konfiguriert.

### Ticket 003 – Maven pom.xml ✅

`mvn package -DskipTests` – EXIT 0, BUILD SUCCESS (52s). Alle 13 Pflicht-Dependencies vorhanden.

### Ticket 004 – application.yml ⚠️ WARN BEHOBEN

| Prüfung | Ergebnis |
|---|---|
| `ddl-auto: none` gesetzt | ✅ |
| `sql.init.mode: always` gesetzt | ✅ |
| `DB_PASSWORD` ohne Default | ✅ |
| `APP_OPERATOR_PASSWORD_HASH` ohne Default | ✅ |
| MAIL-Konfiguration korrekt getrennt | ✅ |
| Hikari Connection-Pool konfiguriert | ✅ |
| **Fail Fast `DB_PASSWORD`** | ✅ – HikariCP `checkFailFast` → App startet nicht |
| **Fail Fast `APP_OPERATOR_PASSWORD_HASH`** | ⚠️ – Kein Fail Fast bei leerem Wert (SecurityConfig noch nicht implementiert) |
| `spring.jpa.open-in-view` Warnung | ✅ Behoben – `open-in-view: false` in `application.yml` ergänzt |

### Ticket 005 – schema.sql ✅

Alle 6 Tabellen vorhanden. FK-Reihenfolge korrekt. UNIQUE-Constraints, Audit-Felder vorhanden. Kein `DROP TABLE`.

---

## Gefundene Fehler

### FEHLER-001 – KRITISCH: BCrypt-Hash-Korrumpierung durch Docker Compose
**Status**: ✅ **BEHOBEN (2026-04-20)**  
Details: `.env` und `.env.example` mit `$$`-Escaping korrigiert. `docker compose config` bestätigt vollen Hash.

---

### FEHLER-002 – BLOCKIEREND: Frontend-Dockerfile schlägt fehl (kein Vue-Projekt)

**Ticket**: `docs/todo/001-docker-compose-dockerfiles-nginx.md`  
**Schwere**: Blockierend – `docker-compose up --build` schlägt fehl, bis Frontend-App vorhanden  
**Status**: ⏳ Offen — Abhängigkeit von Frontend-Scaffolding-Ticket

**Ursache**:  
Das `frontend/`-Verzeichnis enthält nur `Dockerfile` und `nginx.conf`. Es existiert keine `package.json`, kein `package-lock.json`, keine Vue-Quelldateien. `npm ci` in Stage 1 des Dockerfiles schlägt deshalb fehl:
```
npm error The `npm ci` command can only install with an existing package-lock.json
```

**Nachweis**:
```
#21 ERROR: process "/bin/sh -c npm ci --quiet" did not complete successfully: exit code: 1
target frontend: failed to solve: process "/bin/sh -c npm ci --quiet" did not complete successfully: exit code: 1
```

**Bewertung**:  
Das Dockerfile selbst ist korrekt implementiert — es wird funktionieren, sobald die Vue-App scaffoldet ist. Dies ist eine **Abhängigkeit von zukünftigen Frontend-Tickets**, kein Fehler in Ticket 001's Infrastruktur-Implementierung.

**Empfehlung**:  
Ticket 001 bleibt `[TESTING]`. Der letzte Haken (`docker-compose up --build` vollständig) kann erst gesetzt werden, wenn das Frontend-Scaffolding-Ticket abgeschlossen ist.

---

### WARN-001 – `APP_OPERATOR_PASSWORD_HASH` kein Fail Fast bei leerem Wert

**Ticket**: `docs/todo/004-application-yml-konfiguration.md`  
**Schwere**: Mittel – Security-Lücke bis SecurityConfig implementiert  
**Status**: ⏳ Offen — Abhängigkeit von SecurityConfig-Ticket

**Ursache**:  
`${APP_OPERATOR_PASSWORD_HASH}` in `application.yml` hat keinen Default-Wert (korrekt). Wenn die Umgebungsvariable jedoch als leerer String (`""`) gesetzt wird, löst Spring Boot sie zu einem leeren String auf — ohne Fehler. Da noch keine `@ConfigurationProperties`-Klasse oder `@Value`-Injektion für diese Property existiert, liest kein Spring-Bean die Property beim Start. Die Applikation startet mit leerem Hash.

**Reproduktion**:
```bash
docker compose run --rm -e APP_OPERATOR_PASSWORD_HASH="" backend
# → App startet (kein Fail Fast)
```

**Fix** (für SecurityConfig-Ticket):  
Im SecurityConfig muss `@ConfigurationProperties(prefix = "app.security")` mit `@NotEmpty` auf `operatorPasswordHash` gesetzt werden, damit Spring Boot beim Start fehlschlägt.

---

### WARN-002 – `spring.jpa.open-in-view` Warnung
**Status**: ✅ **BEHOBEN** – `spring.jpa.open-in-view: false` in `application.yml` ergänzt (2026-04-20)

---

## Coverage-Übersicht

> Infrastruktur-Tickets haben keine Java/Vue Code-Coverage.  
> Testmethoden: Docker-Laufzeitprüfung, Maven-Build, Filesystem-Check, Fail-Fast-Test.

| Komponente | Geprüfte Aspekte | Ergebnis |
|---|---|---|
| docker-compose.yml | Syntax, Ports, Variablen-Auflösung, DB+Backend Laufzeit | ✅ |
| backend/Dockerfile | Multi-Stage Build, Java 25, Container startet | ✅ |
| frontend/Dockerfile | Build-Versuch | ❌ FEHLER-002 |
| nginx.conf | Strukturprüfung | ✅ |
| files/-Struktur | 7 Ordner + .gitkeep | ✅ |
| pom.xml | mvn package -DskipTests, 13 Dependencies | ✅ |
| application.yml | Laufzeit, Fail-Fast DB, open-in-view Fix | ✅ (mit Fix) |
| schema.sql | 15 Struktur-/Konsistenzprüfungen | ✅ |

---

## Nächste Schritte

- ⚠️ **Ticket 001** → Bleibt `[TESTING]` – FEHLER-002 ist Frontend-Abhängigkeit. Erst nach Frontend-Scaffolding vollständig testbar.
- ✅ **Ticket 002** → `[REVIEW]` gesetzt.
- ✅ **Ticket 003** → `[REVIEW]` gesetzt.
- ⚠️ **Ticket 004** → Zurück auf `[IN_PROGRESS]` – `open-in-view` Fix angewendet; WARN-001 (`APP_OPERATOR_PASSWORD_HASH` Fail Fast) braucht SecurityConfig-Ticket.
- ✅ **Ticket 005** → `[REVIEW]` gesetzt.
