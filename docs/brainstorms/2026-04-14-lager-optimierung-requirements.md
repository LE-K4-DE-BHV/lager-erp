---

## date: 2026-04-14
topic: lager-optimierung-techstack

# Interaktive Lager-Optimierung – Tech-Stack & Implementierungsstrategie

## Problem Frame

Ein manuell betriebener Lagerprozess verursacht unnötige Kapitalbindungskosten und Fehlbestellungsrisiken. Das System soll datengetriebene Bestellvorschläge generieren, aber die finale Entscheidungshoheit beim Menschen lassen ("Human-in-the-Loop"). Die Kernaufgabe dieser Brainstorm-Session war es, die offenen Architekturentscheidungen aus der bestehenden Projektdefinition (`zdocs/projekt_definition.md`) zu treffen, um eine vollständige, umsetzbare Grundlage für die Planung zu schaffen.

**Kontext:** Lern- und Portfolio-Projekt mit dem Ziel, sowohl Fullstack-Kompetenz (Java-Backend + modernes Frontend) als auch Systemarchitektur & DevOps (Docker, containerisierter Betrieb, Batch-Processing) zu demonstrieren.

---

## Finaler Tech-Stack

### Backend


| Komponente            | Technologie                       | Begründung                                                                                                                                   |
| --------------------- | --------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Framework             | Spring Boot 3.x (Java 25)     | Vorgegeben, moderner Stand                                                                                                                   |
| Web-Layer             | Spring Web (REST Controller)      | Saubere API-Trennung für SPA                                                                                                                 |
| Datenzugriff          | Spring Data JPA / Hibernate       | Vorgegeben                                                                                                                                   |
| Datenbank             | PostgreSQL 16 (Docker-Container)  | Vorgegeben, bewährte Wahl                                                                                                                    |
| Sicherheit            | Spring Security (HTTP Basic Auth) | Minimaler Aufwand, echter Login, zeigt Security-Kenntnisse                                                                                   |
| Scheduling            | Spring `@Scheduled` (Cron)        | Kein Spring Batch-Overhead, ausreichend für das Volumen                                                                                      |
| Excel-Parsing         | Apache POI 5.x                    | De-facto-Standard für `.xlsx`-Dateien                                                                                                        |
| CSV-Parsing           | OpenCSV                           | Leichtgewichtig, gut integriert                                                                                                              |
| PDF-Ausgabe           | OpenPDF (LGPL)                    | Kostenlos, keine lizenzrechtlichen Probleme                                                                                                  |
| XML-Ausgabe           | Jakarta XML Binding (JAXB)        | Externe Maven-Dependency erforderlich (`jakarta.xml.bind-api` + Implementierung, z.B. `jaxb-impl`), seit Java 11 nicht mehr im JDK enthalten |
| E-Mail                | Spring Boot Starter Mail          | Vorgegeben                                                                                                                                   |
| Validierung           | Spring Boot Starter Validation    | Standard                                                                                                                                     |
| Boilerplate-Reduktion | Lombok                            | Spart Zeit bei Entities und DTOs                                                                                                             |


### Frontend


| Komponente     | Technologie                | Begründung                                                           |
| -------------- | -------------------------- | -------------------------------------------------------------------- |
| Framework      | Vue.js 3 (Composition API) | Sanftere Lernkurve als React, ideal für Portfolio                    |
| Build-Tool     | Vite                       | Schnell, moderner Standard für Vue-Projekte                          |
| HTTP-Client    | Axios                      | Komfortabler als native Fetch API                                    |
| UI-Komponenten | PrimeVue 4.x               | Reichhaltige Komponentenbibliothek, professionelles Erscheinungsbild |
| Tabellen/Grids | PrimeVue DataTable         | Sortierung, Filterung, Pagination out-of-the-box                     |


### Infrastruktur


| Komponente           | Technologie                                    |
| -------------------- | ---------------------------------------------- |
| Containerisierung    | Docker + Docker Compose                        |
| Container: Datenbank | `postgres:16-alpine`                           |
| Container: Backend   | Custom Dockerfile (Java 25 slim)               |
| Container: Frontend  | `nginx:alpine` (statische Vue-Build-Artefakte) |
| Shared File-Volume   | Host-Mount `./files:/app/files`                |


---

## Systemarchitektur

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Compose                        │
│                                                         │
│  ┌─────────────┐    ┌──────────────┐    ┌───────────┐  │
│  │   frontend  │    │   backend    │    │    db     │  │
│  │  (nginx)    │───▶│ (Spring Boot)│───▶│(Postgres) │  │
│  │  Port 80    │    │  Port 8080   │    │ Port 5432 │  │
│  └─────────────┘    └──────────────┘    └───────────┘  │
│         │                   │                           │
│         │           ┌───────────────┐                   │
│         │           │  ./files Vol. │                   │
│         │           │ Input/        │                   │
│         │           │ Processed/    │                   │
│         │           │ Output/       │                   │
│         │           └───────────────┘                   │
│         │                                               │
└─────────┼───────────────────────────────────────────────┘
          │
     Benutzer-Browser
```

**Kommunikationsfluss:**

- Benutzer → Nginx (Port 80) → Vue.js SPA (statisch)
- Vue.js SPA → Spring Boot REST API (Port 8080) → PostgreSQL
- Batch-Jobs laufen intern in Spring Boot und lesen/schreiben das `./files`-Volume

---

## Anforderungen (Ergänzungen & Präzisierungen)

Die funktionalen Anforderungen aus `zdocs/projekt_definition.md` bleiben vollständig erhalten. Diese Sektion präzisiert offene Punkte:

**Authentifizierung & Benutzerverwaltung**

- R-A1. Das System nutzt Spring Security mit HTTP Basic Auth. Ein einzelner Benutzer (`operator`) wird mit BCrypt-Passwort-Hash als Umgebungsvariable (`APP_OPERATOR_PASSWORD_HASH`) konfiguriert – nicht hard-coded in `application.yml`.
- R-A2. Die `username` aus dem Security-Kontext (`SecurityContextHolder`) wird als `benutzer_id` für den Audit Trail verwendet.
- R-A3. Das Vue-Frontend nutzt **httpOnly-Session-Cookie** statt `sessionStorage`. Spring Security stellt nach erfolgreichem Login ein Session-Cookie aus. Das Frontend sendet das Cookie bei jedem Request automatisch mit. Kein Base64-String im Browser-Storage.
- R-A4. **Scope-Einschränkung:** Das System ist ausschließlich für lokalen/internes Docker-Netz-Betrieb konzipiert. Kein öffentliches Internet-Deployment vorgesehen. TLS/HTTPS ist für diesen Scope bewusst ausgeschlossen.

**Batch-Verarbeitung (ohne Spring Batch)**

- R-B1. Der Dateiimport wird als einfacher `@Scheduled`-Service implementiert. Die Logik (Scan → Parse → Validate → Persist → Move) liegt vollständig im Service-Layer.
- R-B2. Fehlerhafte Einzeldatensätze (Zeilen) werden übersprungen (Skip). Die gesamte Datei wird trotzdem verarbeitet. Fehler werden geloggt.
- R-B3. Klassifizierung nach Verarbeitungsergebnis:
  - **Vollständig fehlerhaft** (z.B. unlesbare Struktur, kein Parsing möglich): → `Processed/Error/`
  - **Teilweise fehlerhaft** (mindestens eine Zeile korrekt verarbeitet, aber Zeilen-Fehler aufgetreten): → `Processed/Warning/`
  - **Vollständig erfolgreich** (keine Fehler): → `Processed/Success/`

**Datenbankschema-Initialisierung**

- R-D1. Das Schema wird über ein manuelles `schema.sql`-Skript angelegt, das Spring Boot beim Start ausführt (`spring.sql.init.mode=always`). Kein Flyway/Liquibase.
- R-D2. Initiale Testdaten können optional via `data.sql` eingefügt werden.

**Manuelle Lagerbewegungen (UI)**

- R-M1. **Wareneingang manuell buchen:** Der Nutzer kann über die UI eine Wareneingangsbuchung vornehmen. Pflichtfelder: Artikel (Dropdown), Menge, Datum. Voraussetzung: Es muss eine offene Bestellung für den gewählten Artikel existieren. Ohne offene Bestellung ist die Buchung gesperrt (Fehlermeldung: "Keine offene Bestellung für diesen Artikel vorhanden").
- R-M2. **Bestellung schließen:** Nach erfolgter Wareneingangsbuchung fragt das System: "Soll die verknüpfte Bestellung als vollständig geliefert markiert werden?" (Ja / Nein). Bei "Ja" wechselt der Bestellstatus auf `GELIEFERT`.
- R-M3. **Warenausgang manuell buchen:** Der Nutzer kann über die UI eine Warenausgangsbuchung vornehmen. Pflichtfelder: Artikel (Dropdown), Menge, Buchungstyp (Dropdown, Pflicht), optionaler Freitext-Grund. Buchungstypen: `Verbrauch intern` / `Verkauf` / `Verlust/Schwund` / `Retoure`.
- R-M4. Beide Buchungstypen (Eingang + Ausgang) aktualisieren den Bestand transaktionssicher und werden als Einträge in der Transaktionshistorie gespeichert (gleiche Tabelle wie Batch-Transaktionen, mit Quelle `MANUELL`).
- R-M5. Manuelle Warenausgänge fließen in die Verbrauchsanalyse des Reorder-Jobs ein – sie werden für die Tagesverbrauchs-Berechnung gleichwertig zu Batch-Warenausgängen behandelt.

**Artikel anlegen & bearbeiten (UI)**

- R-N1. **Neues Material anlegen:** Über ein UI-Formular kann ein neuer Artikel vollständig angelegt werden. Pflichtfelder: Artikelnummer (eindeutig), Bezeichnung, Mengeneinheit, Warengruppe, Hauptlieferant (Dropdown aus bestehenden Lieferanten). Optionale Bestellparameter direkt im selben Formular: Sicherheitsbestand, Bestellpunkt, Standardbestellmenge.
- R-N2. Die Artikelnummer wird bei der Anlage auf Eindeutigkeit geprüft. Bei Duplikat: Inline-Fehlermeldung, kein Speichern.
- R-N3. **Bestehenden Artikel bearbeiten:** Alle Felder eines bestehenden Artikels (Stammdaten + Bestellparameter) können über die UI editiert werden.
- R-N4. Manuell angelegte Artikel verhalten sich für Reorder-Analyse, Bestellvorschläge und Dashboard identisch zu Batch-importierten Artikeln.

**Importdokument-Spezifikation**

- R-I1. Das System definiert ein festes Spaltenformat (Schema) für jede Batch-Eingabedatei. Das Schema ist verbindlich – abweichende Dateien werden als fehlerhaft eingestuft (R-B3).
- R-I2. Das Planungsdokument (und die spätere Nutzerdokumentation) muss für jede Eingabedatei ein konkretes Beispiel enthalten – sowohl als CSV-Textbeispiel als auch als Excel-Tabelle mit korrekten Spaltenköpfen. Betrifft: Stammdaten-Artikel, Stammdaten-Lieferanten, Transaktionen-Eingang, Transaktionen-Ausgang.
- R-I3. Die Spaltenköpfe in den Dateien müssen exakt mit den definierten Schema-Namen übereinstimmen (case-insensitive Matching ist erlaubt).

**Bestellungsausgabe**

- R-O1. Nach manueller Freigabe generiert das System **beide** Ausgabeformate:
  - PDF (via OpenPDF): Visuell ansprechend, druckbar, für den Einkäufer.
  - XML (via JAXB): Maschinenlesbar, für potenzielle System-Integration.
- R-O2. Beide Dateien werden im Ordner `Output/Orders/` abgelegt. Dateiname-Schema: `Bestellung_{BestellNr}_{Datum}.{pdf|xml}`.
- R-O3. Die PDF-Bestellung enthält vollständige Felder: Bestellnummer, Datum, Lieferant (Name + Kontakt), Artikel (Nr., Bezeichnung, Menge, Einheit), Einkaufspreis je Einheit, Gesamtbetrag, gewünschtes Lieferdatum, Freitext-Notiz, Erstellt-von (Benutzer), Firmenlogo-Platzhalter (konfigurierbar).

**App-Navigation & Dashboard-Struktur**

- R-P1. **Dashboard als zentraler Hub:** Das Dashboard ist die Startseite der App. Alle Aktionsbereiche sind über Quick-Links/Kacheln vom Dashboard erreichbar. Es gibt keine permanente Sidebar.
- R-P2. **Dashboard-Kacheln:** Vom Dashboard aus erreichbar: "Lagerbewegung buchen" (Wareneingang/Ausgang), "Artikel verwalten", "Lieferanten", "Transaktionshistorie", "Bestellhistorie".
- R-P3. **Dashboard KPI-Summary:** Oben im Dashboard ein kompakter Überblick: Anzahl offener Bestellvorschläge, davon Anzahl kritischer (unter Sicherheitsbestand), Zeitpunkt des letzten Reorder-Analyse-Laufs.
- R-P4. **Manueller Reorder-Trigger:** Im Dashboard gibt es einen Button "Analyse jetzt ausführen", der den Reorder-Analyse-Job sofort (synchron oder asynchron) anstößt. Nützlich nach großen Buchungen.
- R-P5. **Täglicher Bestandsbericht:** Das System generiert täglich automatisch einen Bestandsbericht (PDF) und legt ihn in `Output/Reports/` ab. Inhalt: alle aktiven Artikel mit aktuellem Bestand, Bestellpunkt, Sicherheitsbestand, Status (Normal/Kritisch).

**Bestellvorschlag-Lebenszyklus**

- R-L1. Ein Bestellvorschlag durchläuft folgende Status: `VORSCHLAG` → `BESTELLT` → `GELIEFERT` (oder `IGNORIERT`).
- R-L2. **Sichtbarkeit im Dashboard:** Ein Vorschlag verbleibt im Dashboard, bis er den Status `GELIEFERT` oder `IGNORIERT` erreicht. Im Status `BESTELLT` erscheint er weiterhin, aber als "In Lieferung" (visuell gedämpft, z.B. grau).
- R-L3. **Ignorieren:** Der Nutzer kann einen Bestellvorschlag permanent ignorieren. Aktion: Button "Ignorieren" pro Zeile → Bestätigungsdialog → Status wechselt auf `IGNORIERT`, Vorschlag verschwindet aus dem Dashboard. Der Reorder-Job erstellt für diesen Artikel **keinen neuen** Vorschlag, solange Bestand > Bestellpunkt.
- R-L4. **Automatisches Schließen bei Batch-Wareneingang:** Wenn ein Wareneingang per Batch-Import verarbeitet wird und Artikelnummer sowie Menge zu einer offenen Bestellung (`BESTELLT`) passen, setzt das System die Bestellung automatisch auf `GELIEFERT`.
- R-L5. **Manuelles Schließen:** Der manuelle Wareneingang (R-M2) setzt die Bestellung auf `GELIEFERT`, wenn der Nutzer dies bestätigt.

**Artikel-Stammdaten-Erweiterungen**

- R-S1. **Einkaufspreis:** Jeder Artikel hat ein optionales Feld `Einkaufspreis` (Preis pro Mengeneinheit). Wird im Artikel-Formular gepflegt und für die Gesamtbetragsberechnung im PDF verwendet.
- R-S2. **Bestellpunkt manuell:** Sicherheitsbestand und Bestellpunkt werden ausschließlich manuell vom Nutzer im Artikel-Formular eingegeben. Das System berechnet keinen Vorschlag dafür.
- R-S3. **Artikel deaktivieren:** Ein Artikel kann über die UI auf `INAKTIV` gesetzt werden. Inaktive Artikel: verschwinden aus Reorder-Analyse und Dashboard, bleiben aber in der Datenbank erhalten (Transaktionshistorie bleibt vollständig).

**Lieferanten-Verwaltung (UI)**

- R-V1. **Lieferantenliste:** Vom Dashboard aus (Kachel "Lieferanten") kann der Nutzer alle Lieferanten einsehen. Angezeigte Felder: ID, Name, Kontaktdaten, Lead Time (Lieferzeit in Tagen).
- R-V2. **Lieferant bearbeiten:** Bestehende Lieferanten können über die UI editiert werden (Name, Kontaktdaten, Lead Time). Kein Anlegen neuer Lieferanten über die UI (nur via Batch-Import).

**Transaktionshistorie (UI)**

- R-T1. **Transaktionshistorie-Seite:** Erreichbar vom Dashboard als eigene Seite. Zeigt alle Warenbewegungen chronologisch: Datum, Artikelnummer/-bezeichnung, Typ (Wareneingang/Warenausgang/Inventurkorrektur), Menge, Buchungstyp, Quelle (Batch/Manuell), optional: Bezug zur Bestellung.
- R-T2. Die Seite unterstützt Filterung nach Artikel und Zeitraum.



---

## Dateistruktur (Batch-Volumes)

```
files/
├── Input/
│   ├── Stammdaten/          ← Excel/CSV-Importe: Artikel & Lieferanten
│   └── Transaktionen/       ← Excel/CSV-Importe: Wareneingänge & -ausgänge
├── Processed/
│   ├── Success/             ← Vollständig erfolgreich verarbeitete Dateien
│   ├── Warning/             ← Teilweise verarbeitet (mind. 1 Zeile fehlerhaft)
│   └── Error/               ← Nicht verarbeitbar (Dateistruktur unlesbar)
└── Output/
    ├── Reports/             ← Berichte (z.B. Bestandsübersichten)
    └── Orders/              ← Finale Bestellungen (.pdf + .xml)
```

---

## Benutzerfluss – Bestellfreigabe & Lebenszyklus

```
Reorder-Job läuft (nächtlich oder manuell angestoßen)
        │
        ▼
Bestand <= Bestellpunkt?  UND  kein offener Vorschlag?
        │ JA
        ▼
Neuer Bestellvorschlag (Status: VORSCHLAG)
        │
        ▼
┌──────────────────────────────────────────────────────┐
│  Dashboard                                            │
│  KPI-Summary: X Vorschläge, Y kritisch, Letzter Lauf │
│  Tabelle: VORSCHLAG + BESTELLT (=In Lieferung)        │
│  Gelb = Normal / Rot = Kritisch / Grau = In Lieferung │
└──────────────────────────────────────────────────────┘
        │
   ┌────┼────────────────────┐
   ▼    ▼                    ▼
[Schnell- [Bearbeiten]   [Ignorieren]
bestellen]     │               │
   │           ▼               ▼
   │    Bearbeitungs-Modal   Bestätigungsdialog
   │    [Bestätigen/Abbrechen] → Status: IGNORIERT
   │           │               (verschwindet aus Liste)
   └─────┬─────┘
         ▼
  Status: BESTELLT
  PDF + XML generiert
  Bleibt als "In Lieferung" sichtbar
         │
         ▼
  Wareneingang kommt (manuell oder Batch)
         │
         ▼
  Passt Artikel + Menge zu offener Bestellung?
         │ JA
         ▼
  Status: GELIEFERT
  Vorschlag verschwindet aus Dashboard
```

---

## Erfolgs-Kriterien

- Ein Artikel, dessen Bestand den Bestellpunkt unterschreitet, erscheint spätestens nach dem nächsten Scheduler-Lauf im Dashboard.
- Eine Schnellbestellung kann in unter 3 Klicks ausgelöst werden.
- Das PDF und die XML-Datei sind im `Output/Orders/`-Ordner auffindbar nach der Freigabe.
- Ein CSV-Import mit 100 Datensätzen, von denen 3 fehlerhaft sind, verarbeitet die 97 gültigen Zeilen korrekt und verschiebt die Datei in `Processed/Warning/` (nicht Error).
- Alle Bestellfreigaben sind im Audit Trail mit Benutzer und Zeitstempel nachvollziehbar.
- Ein manueller Warenausgang (z.B. Typ "Verbrauch intern") senkt den Bestand sofort und erscheint beim nächsten Reorder-Job in der Verbrauchsanalyse.
- Ein manueller Wareneingang ohne offene Bestellung zeigt eine klare Fehlermeldung statt eine fehlerhafte Buchung zu ermöglichen.
- Ein neu manuell angelegter Artikel erscheint nach dem nächsten Reorder-Lauf im Dashboard, falls sein Bestand den Bestellpunkt unterschreitet.
- Ein ignorierter Bestellvorschlag kommt beim nächsten Reorder-Job **nicht** wieder (solange Bestand > Bestellpunkt).
- Nach Auslösung einer Bestellung bleibt der Vorschlag als "In Lieferung" (grau) sichtbar – er verschwindet erst nach der Wareneingangsbuchung (`GELIEFERT`).
- Ein Batch-Wareneingang, der zu einer offenen Bestellung passt, setzt diese automatisch auf `GELIEFERT`.
- Der Button "Analyse jetzt ausführen" startet die Reorder-Analyse unmittelbar und aktualisiert das Dashboard.
- Täglich liegt ein aktueller Bestandsbericht-PDF in `Output/Reports/`.

---

## Scope Boundaries

- Keine Multi-User-Verwaltung (Rollen, Registrierung, Passwort-Reset). Ein Benutzer, konfiguriert in `application.yml`.
- Kein Spring Batch. Einfache `@Scheduled`-Services sind ausreichend und leichter zu verstehen und warten.
- Kein Flyway/Liquibase. Schema-Migration via `schema.sql`.
- Kein separater Message-Broker (Kafka, RabbitMQ). Alle Prozesse laufen synchron innerhalb der Spring Boot Applikation.
- Keine automatischen Bestellungen. Der Mensch bleibt immer in der Freigabe-Schleife.
- Kein Real-Time Push (WebSocket). Das Dashboard wird bei Benutzeraktion aktualisiert (Pull-Modell via API-Calls).
- **Artikel-Anlage:** Sowohl via Batch-Import als auch manuell per UI möglich.
- **Lieferanten-Anlage:** Ausschließlich via Batch-Import. Bearbeiten bestehender Lieferanten ist per UI möglich.
- **Bestellpunkt:** Wird ausschließlich manuell vom Nutzer gesetzt – keine automatische Berechnung durch das System.
- **Artikel-Löschung:** Kein physisches Löschen. Nur Deaktivieren (Status `INAKTIV`).
- **Teillieferungen:** Nicht explizit unterstützt. Wareneingang schließt die Bestellung vollständig (`GELIEFERT`). Teilmengen werden im Scope nicht behandelt.

---

## Key Decisions


| Entscheidung         | Wahl                                                          | Begründung                                                                                          |
| -------------------- | ------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| Frontend-Architektur | Vue.js 3 SPA (Nginx-Container, Reverse Proxy zu Backend)      | Fullstack-Portfolio-Ziel; klare Trennung Backend/Frontend; CORS-freie Kommunikation via Nginx-Proxy |
| Batch-Engine         | `@Scheduled` + Services                                       | Kein Spring Batch-Overhead, für das Volumen ausreichend                                             |
| DB-Schema-Management | Manuelles `schema.sql`                                        | Einfachheit; kein Migrations-Framework nötig                                                        |
| Authentifizierung    | Spring Security Basic Auth (1 User) + httpOnly Session-Cookie | XSS-sicher, kein Credential-Storage im Browser, Audit-Trail-Basis                                   |
| Bestellungsausgabe   | PDF (OpenPDF) + XML (JAXB)                                    | Zeigt beide Anwendungsfälle: Mensch + Maschine                                                      |
| PDF-Bibliothek       | OpenPDF (statt iText 7)                                       | LGPL-Lizenz, keine kommerziellen Einschränkungen                                                    |


---

## Dependencies / Assumptions

- Die Excel-/CSV-Dateien haben ein definiertes, stabiles Spaltenformat. Das Parsing-Schema muss einmalig konfiguriert und dokumentiert werden.
- Verarbeitete Dateien werden nach Datum gruppiert abgelegt: `Processed/{Success|Warning|Error}/YYYY-MM-DD/`.
- Der einzelne Benutzer (`operator`) wird als bekannt vorausgesetzt. Es gibt keinen Self-Service-Registrierungsprozess.
- Das Datenvolumen rechtfertigt keine horizontale Skalierung. Vertikale Skalierung (Container-Resources) ist ausreichend.
- **Geheimnisse:** Alle sensitiven Werte (Passwort-Hash, SMTP-Credentials, DB-Passwort) werden über Umgebungsvariablen in Docker Compose (`.env`-Datei) übergeben. `.env` ist in `.gitignore`. `application.yml` referenziert nur Platzhalter (`${VAR_NAME}`).
- Der SMTP-Server für E-Mail-Alerts wird als Umgebungsvariable konfiguriert. Er ist nicht Teil des Docker-Setups.
- `**spring.jpa.hibernate.ddl-auto=none`** muss in `application.yml` gesetzt sein, da das Schema ausschließlich via `schema.sql` initialisiert wird.

---

## Outstanding Questions

### Resolve Before Planning

*(Alle Blocking-Fragen wurden beantwortet. Planung kann beginnen.)*

### Deferred to Planning

- **[Betrifft R-A3][Technisch]** Nginx als Reverse Proxy zu Spring Boot (Port 8080) konfigurieren: alle `/api/**`-Anfragen werden vom Nginx-Container weitergeleitet. Damit entfällt das CORS-Problem (Same-Origin aus Browser-Sicht).
- **[Betrifft R_2.3.2][Technisch]** Welcher Zeitraum historischer Verbräuche soll für die Tagesverbrauchs-Berechnung herangezogen werden? (z.B. letzte 30 Tage, letzte 90 Tage) – konfigurierbar in `application.yml`.
- **[Betrifft R-I2][Technisch]** Genaue Spaltennamen und -reihenfolge für alle 4 Datei-Typen (Stammdaten-Artikel, Stammdaten-Lieferanten, Transaktionen-Eingang, Transaktionen-Ausgang) – inkl. Pflichtfelder vs. optional. Im Planungsdokument als konkrete Beispiel-CSV/Excel-Tabellen ausarbeiten.
- **[Betrifft R-B1][Technisch]** Doppelverarbeitung verhindern: Strategie definieren, falls eine Datei mit gleichem Namen erneut in `Input/` abgelegt wird (z.B. Dateiname-Hashing oder Datenbank-Track).
- **[Betrifft R-L3][Technisch]** Genauer Mechanismus für "kein neuer Vorschlag nach IGNORIERT": Entweder Flag am Artikel oder Status-Check im Reorder-Job (wenn bereits ein IGNORIERT-Vorschlag jünger als X Tage existiert).
- **[Betrifft R-P4][Technisch]** Manueller Reorder-Trigger: synchron (Nutzer wartet) oder asynchron mit Progress-Anzeige? Abhängig von Laufzeit des Jobs.

---

## Nächste Schritte

→ `/ce:plan` für die strukturierte Implementierungsplanung

Alle Blocking-Fragen sind beantwortet. Das Dokument ist bereit für die Planung.