# Anforderungsdefinition: Interaktive Lager-Optimierung

## 1. Projekthintergrund & Zielsetzung

Hintergrund: Die aktuelle Lagerhaltung ist ineffizient, was zu unnötig hohen Kapitalbindungskosten führt. Bisherige Bestellprozesse basieren oft auf Erfahrungswerten und manuellen Prüfungen, ohne systematische Analyse historischer Verbrauchsdaten oder aktueller Bestände.

Ziel: Ziel dieses Projekts ist die Entwicklung einer containerisierten Web-Anwendung, die Bestands- und Stammdaten automatisiert analysiert, um optimale Bestellzeitpunkte und -mengen auf Basis der Bestellpunktmethode zu ermitteln. Um Fehlbestellungen zu vermeiden, darf das System Bestellungen niemals automatisch auslösen. Stattdessen werden alle Bestellvorschläge in einem interaktiven Dashboard visualisiert. Dort kann ein Benutzer die Vorschläge prüfen, bei Bedarf in einer bearbeitbaren Bestellvorlage anpassen und die Bestellung schließlich manuell per Klick freigeben.

Nutzen:

- Minimierung der Lagerbestände und Reduzierung der Kapitalbindung.
- Sicherstellung der Lieferfähigkeit durch datengetriebene Nachbestellung.
- Effizienzsteigerung im Einkauf durch automatisierte Vorschläge und interaktive Freigabe-Prozesse.
- Vermeidung von Fehlbestellungen durch den „Human-in-the-Loop“-Ansatz.

## 2. Funktionale Anforderungen

Diese Anforderungen beschreiben, was das System leisten muss.

### 2.1 Stammdaten-Management (Items & Lieferanten)

R_2.1.1 Artikelerfassung: Das System muss Artikeldaten (Stammdaten) speichern und verwalten. Notwendige Datenfelder: Artikelnummer (eindeutig), Bezeichnung, Mengeneinheit, Warengruppe, Hauptlieferant (FK zu Lieferanten).

R_2.1.2 Lieferantenerfassung: Das System muss Lieferantendaten speichern. Notwendige Datenfelder: Lieferanten-ID (eindeutig), Name, Kontaktdaten, durchschnittliche Lieferzeit (Lead Time in Tagen).

R_2.1.3 Importfunktion (Batch): Es muss ein automatisierter Batch-Prozess implementiert werden, der Stammdaten (Artikel und Lieferanten) aus Excel- oder CSV-Dateien aus einem definierten Eingangsordner (Input/Stammdaten) einliest und in die Datenbank schreibt.

### 2.2 Bestandsdaten & Transaktionen

R_2.2.1 Aktueller Bestand: Das System muss den aktuellen physischen Lagerbestand pro Artikel speichern und anzeigen können.

R_2.2.2 Erfassung von Lagerbewegungen (Batch): Es muss ein automatisierter Batch-Prozess implementiert werden, der Transaktionen aus Excel- oder CSV-Dateien aus einem Eingangsordner (Input/Transaktionen) einliest und den Bestand aktualisiert.

- Wareneingang: Erhöht den Bestand (Referenz auf Bestellung/Lieferant).
- Warenausgang (Verbrauch): Verringert den Bestand (interner Verbrauch oder Verkauf).

R_2.2.3 Transaktions-Schnittstelle (Real-Time): Die Web-UI muss Funktionen bereitstellen, um kritische Bestandsanpassungen (z.B. Inventurkorrekturen) direkt und in Echtzeit zu erfassen.

### 2.3 Automatisierte Analyse & Bestellpunkt-Logik (Hintergrund-Prozess)

R_2.3.1 Definition von Bestellparametern: Für jeden Artikel müssen spezifische Parameter für die Bestelllogik hinterlegt werden:

- Sicherheitsbestand (Safety Stock): Mindestmenge zur Überbrückung von Engpässen.
- Bestellpunkt (Reorder Point): Der Bestandslevel, bei dessen Erreichen eine Nachbestellung vorgeschlagen wird. (Berechnung: Tagesverbrauch * Lieferzeit + Sicherheitsbestand).
- Standard-Bestellmenge (StdOrderQty): Die standardmäßig zu bestellende Menge (z.B. Mindestbestellmenge des Lieferanten).

R_2.3.2 Automatische Analyse (Scheduling): Ein automatisierter Hintergrundprozess (z.B. nächtlich) analysiert die aktuellen Bestände und historischen Verbräuche.

- Verbrauchsanalyse: Berechnung des durchschnittlichen Tagesverbrauchs pro Artikel auf Basis historischer Warenausgänge.
- Prüfung auf Bestellvorschlag: Wenn Aktueller Bestand <= Bestellpunkt, wird ein neuer Bestellvorschlag generiert und in der Datenbank gespeichert.

R_2.3.3 Statuts-Überwachung: Das System überwacht den Status von Artikeln. Wenn für einen Artikel bereits ein offener Bestellvorschlag existiert, darf kein neuer Vorschlag für denselben Artikel generiert werden, um Doppelungen zu vermeiden.

### 2.4 Interaktives Dashboard & Bestellprozess (Web-UI)

Dies ist der Kern-Interaktionsbereich für den Benutzer.

R_2.4.1 Zentrales Bestell-Dashboard: Die Web-Oberfläche muss alle aktiven Bestellvorschläge prominent im Hauptdashboard listen.

R_2.4.2 Visualisierung von Vorschlägen:

- Tabellarische Darstellung: Alle Artikel, die bestellt werden müssen. Enthaltene Spalten: Artikelname/-nummer, Status (z.B. Kritisch), aktueller Bestand, Bestellpunkt, Lieferant, vorgeschlagene Standard-Bestellmenge.
- Priorisierung: Kritische Bestände (z.B. Unterschreitung des Sicherheitsbestands) müssen visuell hervorgehoben werden (z.B. durch Warnfarben).

R_2.4.3 Interaktive Bestellvorlage: Für jeden Bestellvorschlag muss das System eine bearbeitbare Bestellvorlage (Template) generieren.

R_2.4.4 Benutzergesteuerte Bestalleuslöung (Aktion): Der Benutzer muss direkt im Dashboard auf einen Bestellvorschlag reagieren können. Es dürfen niemals automatisch Bestellungen ausgelöst werden. Stattdessen stehen dem Nutzer folgende Aktionen zur Verfügung:

- Aktion A: Schnellbestellung (Quick Order): Ein direkter Klick auf eine Schaltfläche (z.B. „Jetzt Bestellen“), die die Bestellung sofort mit den vorausgefüllten Standardwerten (Menge, Lieferant) auslöst.
- Aktion B: Bearbeiten & Bestellen: Öffnen der interaktiven Bestellvorlage, um Parameter (Menge anpassen, Lieferant ändern, gewünschtes Lieferdatum setzen) vor der endgültigen manuellen Freigabe zu bearbeiten.

R_2.4.5 Bestellhistorie & Status: Nach der Auslösung durch den Benutzer (Aktion A oder B) wird die Bestellung in einer Historientabelle gespeichert und der Status des Artikels im Dashboard von „Vorschlag“ auf „Bestellt“ (inkl. Zeitstempel und Benutzer-ID) aktualisiert.

R_2.4.6 Ausgabe der finalen Bestellung: Nach der manuellen Auslösung generiert das System die finale Bestellung (z.B. als PDF oder strukturiertes XML) und legt diese im Output/Orders-Ordner zur Weiterleitung an den Lieferanten ab.

## 3. Nicht-funktionale Anforderungen

Diese Anforderungen beschreiben, wie das System seine Funktionen erfüllen muss.

### 3.1 Technische Plattform & Stack

R_3.1.1 Framework: Die Anwendung wird mit Spring Boot entwickelt.

R_3.1.2 Deployment: Die gesamte Anwendung, einschließlich der Datenbank und Web-Oberfläche, muss in Docker-Containern laufen.

R_3.1.3 Datenbank: Es wird eine containerisierte Datenbank (z.B. PostgreSQL, analog zum vorherigen Projekt) verwendet, ohne lokale Installation auf dem Host-System.

R_3.1.4 Benutzeroberfläche (UI): Die Web-Oberfläche wird mit Spring Boot (z.B. Thymeleaf für serverseitiges Rendering oder eine integrierte SPA) entwickelt. Sie muss im Docker-Netzwerk erreichbar sein.

### 3.2 Betriebsmodell & Automatisierung

R_3.2.1 Hybrid-Betrieb: Das System läuft hybrid:

- Unattended Batch-Prozesse: Datenimport und Bestandsanalyse laufen vollautomatisch und zeitgesteuert im Hintergrund (Scheduling via Spring @Scheduled).
- Attended UI: Bestellfreigabe, -bearbeitung und Bestandsanpassungen erfolgen manuell und interaktiv über die Web-Oberfläche durch einen Benutzer.

R_3.2.2 Scheduling-Konfiguration: Die Frequenz der Batch-Prozesse (z.B. täglicher Import und Analyse) muss konfigurierbar sein (z.B. via Spring @Scheduled Cron-Ausdrücke).

### 3.3 Datenintegration & -handling

R_3.3.1 Dateibasierte Batch-Schnittstellen: Der automatische Datenaustausch für Stammdaten und Transaktionen erfolgt ausschließlich über Excel/CSV-Dateien in einer definierten Ordnerstruktur.

R_3.3.2 Ordnerstruktur (Batch): Es ist eine klare Ordnerstruktur zu definieren (analog zum vorherigen Projekt): Input/[Typ], Processed/Success, Processed/Error, Output/Reports, Output/Orders.

R_3.3.3 Fehlertoleranz: Wenn eine Datei nicht eingelesen werden kann, darf der gesamte Prozess nicht abbrechen. Die fehlerhafte Datei wird verschoben, und der Fehler wird protokolliert.

R_3.3.4 Real-Time Integration (Web): Bestellfreigaben und manuelle Bestandsanpassungen über die Web-UI müssen sofort und transaktionssicher in der Datenbank gespeichert werden.

### 3.4 Performance & Skalierbarkeit

R_3.4.1 Datenvolumen: Das System muss Stammdaten für X Artikel, Y Lieferanten und Z Lagerbewegungen pro Tag performant verarbeiten können. (Konkrete Zahlen sind im Projektverlauf zu definieren).

R_3.4.2 Antwortzeiten (UI): Das Dashboard und die Bestellvorlagen müssen performant laden (Antwortzeit < 2 Sekunden für Standard-Anfragen).

### 3.5 Sicherheit & Logging

R_3.5.1 Zugriffskontrolle: Da keine Benutzerverwaltung vorgesehen ist, bezieht sich Sicherheit auf den Dateizugriff und die Datenbankverbindung innerhalb des Docker-Netzwerks.

R_3.5.2 Detailliertes Logging: Alle Prozessschritte, erfolgreiche Importe/Exporte sowie auftretende Fehler (technisch und fachlich) müssen in Logfiles und optional in einer Protokolltabelle in der Datenbank festgehalten werden.

R_3.5.3 Fehlerbenachrichtigung: Kritische Fehler (z.B. DB-Verbindungsausfall) sollten eine proaktive Benachrichtigung (z.B. E-Mail) auslösen.

R_3.5.4 Audit Trail (Bestellungen): Alle Bestellungen, die von Benutzern manuell ausgelöst werden, müssen transaktionssicher mit Zeitstempel und Benutzer-ID protokolliert werden (Audit Trail).

## Detaillierte Feature-Planung

Wir strukturieren die Entwicklung in klare, aufeinander aufbauende Phasen und Komponenten, um eine transactional-sichere und performante Umsetzung zu gewährleisten.

### Phase 1: Infrastruktur & Docker-Setup (Das Fundament)

Das Ziel ist es, die Entwicklungsumgebung und die Container-Orchestrierung so aufzusetzen, dass die Anwendung portabel ist und die Datenbank nur im Container lebt.

#### F1.1: Docker Orchestrierung (docker-compose.yml):

Beschreibung: Definition der Laufzeitumgebung für PostgreSQL und Spring Boot.

Entwickler-Tasks:

- Erstellen einer docker-compose.yml.
- Dienst db definieren: Image postgres:16-alpine.
- Ports mappen (5432:5432).
- Umgebungsvariablen für DB, User, Passwort setzen.
- Volumes definieren: db_data:/var/lib/postgresql/data (für DB-Persistenz) und host-gemountete Verzeichnisse für die shared File-Ordner-Struktur (z.B. volumes: - ./files:/app/files), damit User/Systeme Dateien dort ablegen können.

#### F1.2: Spring Boot Projekt-Setup & Config:

Beschreibung: Initialisierung des Projekts und zentrale Konfiguration.

Entwickler-Tasks:

- Projekt generieren (Spring Initializr: Batch, JPA, Postgres, Validation, Mail, Web/Thymeleaf oder SPA-Integration).
- application.yml konfigurieren: DB-Verbindungsdaten, Hibernate-Validierung, Konfiguration der Shared-File-Pfade (konsistent mit Docker-Mounts).

#### F1.3: Datenbank-Initialisierung (SQL):

Beschreibung: Bereitstellung der SQL-Skripte zur Erstellung der Tabellenstruktur beim DB-Start.

Entwickler-Tasks:

- SQL-Skripte für Tabellen: kostenarten, kosten, lieferanten, artikel, transaktionen, bestellvorschlaege, bestellungen (mit Status, Zeitstempel, Benutzer-ID für Audit-Trail).
- SQL-Skript für View ist_plan_kostenvergleich.

### Phase 2: Domain Modell & Gemeinsame Services

Hier definieren wir die Datenstrukturen und Kernlogiken, die sowohl von den Batch-Jobs als auch von der Web-UI genutzt werden.

#### F2.1: JPA Entities & Repositories (Shared):

Beschreibung: Abbildung der DB-Tabellen auf Java-Objekte.

Entwickler-Tasks:

- Entities für alle Tabellen erstellen (JPA-Annotationen, Validierung).
- Repositories erstellen (JpaRepository) für alle Entities.
- Zusätzliche Repository-Methoden: findActiveProposals(), createOrderFromProposal() (logisch im Service), saveAdjustedOrder(), getOrderHistoryForAudit(), calculateStockAdjustmentFromTransactions().

#### F2.2: Gemeinsame Services (@Service):

Beschreibung: Zentrale Logiken für Datei- und Lagerhandling.

Entwickler-Tasks:

- FileService: Robuste Methoden für Datei-Scan mit Pattern, Verschieben verarbeiteter Dateien (konsistent mit Ordnerstruktur R_3.3.2), Kopieren von Vorlagen, Generierung finaler Bestellungen/Berichte in Output-Ordnern. Muss shared Pfade korrekt behandeln.
- InventoryService: Transaction-sichere Methoden zur direkten (Real-Time R_2.2.3) und batch-getriebenen Bestandsaktualisierung (Item.currentStock), Verwaltung von Reorder-Parametern, Artikel/Lieferanten-Management.
- OrderProposalService: Transaction-sichere Generierung von Bestellvorschlägen basierend auf Bestellpunkten, Abruf aktiver Vorschläge für Dashboard (R_2.4.1), Verwaltung des Vorschlags-Status (z.B. VORSCHLAG, IN_BEARBEITUNG, BESTELLT, IGNORIERT R_2.4.2 & R_2.4.5), Generierung einer Bestellung aus Vorschlag (Schnellbestellung/Bearbeitung).
- MailAlertService: Sendet Alerts bei kritischen Fehlern (R_3.5.3).

### Phase 3: Spring Batch Jobs (Hintergrund-ETL)

Diese Phase realisiert die robusten, zeitgesteuerten Batch-Prozesse.

#### F3.1: Stammdaten & Transaktionen Imports (Batch R_2.1.3 & R_2.2.2):

Beschreibung: Robuster Import von Stammdaten (R_2.1.3) und Lagerbewegungen (IN/OUT, R_2.2.2) aus Excel/CSV.

Entwickler-Tasks:

- Klassen für Stammdaten- und Transaktionen-Import-Jobs erstellen.
- ItemReader für Excel/CSV (validiert Struktur, extrahiert Daten).
- ItemProcessor: Validiert Kostenart/FKs, konvertiert Stammdaten zu Entities bzw. erstellt Transaktionen und berechnet Bestandsaktualisierung.
- Pre-Import-Validierungs-Step oder Tasklet, um sicherzustellen, dass keine Doppelverarbeitung oder kritischen Fehler unbemerkt bleiben.
- Standard JpaItemWriter zum Speichern.
- Wichtig: Der Transaktionen-Job muss nach dem Speichern der Transaktionen auch den Lagerbestand transaktionssicher aktualisieren – am besten über einen DB-Trigger nach Insert in Transaktionen oder konzeptionell robust nach Chunk-Schreiben gesteuert. Letzteres ist für Spring Batch chunks tricky, daher ist ein Trigger robuster innerhalb der chunk-transaction.

#### F3.2: Reorder-Analyse (Batch R_2.3.2):

Beschreibung: Nächtlicher Lauf zur Berechnung von Verbräuchen und Generierung von Bestellvorschlägen.

Entwickler-Tasks:

- Klasse ReorderAnalysisJob erstellen.
- ItemReader: Liest alle Artikel (JpaCursorItemReader).
- ItemProcessor: Ruft InventoryService auf, um historischen Verbrauch/Tagesverbrauch zu berechnen, Bestellpunkt zu ermitteln, mit Bestand vergleichen und prüft, ob bereits offener Vorschlag R_2.3.3 existiert.
- ItemWriter: Speichert neue Bestellvorschlaege.

#### F3.3: Job Orchestrierung & Zeitplanung (Unattended R_3.2.1-2):

Beschreibung: Steuerung des automatischen Batch-Betriebs.

Entwickler-Tasks:

- Hauptklasse @EnableScheduling.
- JobScheduler-Klasse: @Scheduled(cron = "${scheduling.xxx}")-Methoden zum Starten aller Jobs mit konfigurierbaren Cron-Ausdrücken.

### Phase 4: Web-Interface & REST API (Der interaktive Teil)

Hier entwickeln wir das Backend für das Dashboard und die interaktiven Funktionen. Da wir eine interaktive Benutzerentscheidung und Bearbeitung benötigen, ist ein Web-Frontend (z.B. als SPA oder integriert mit Thymeleaf) essenziell. Für ein optimales Benutzererlebnis empfehle ich ein modernes SPA-Frontend, das über eine REST API kommuniziert.

#### F4.1: Web API REST Controller:

Beschreibung: Endpoints für das Dashboard und Benutzeraktionen.

Entwickler-Tasks:

- DashboardController: GET-Endpoints zum Abrufen aller aktiven Bestellvorschlaege (sortiert/priorisiert R_2.4.1), R_3.4.2 Antwortzeiten beachten.
- InventoryApiController: GET/POST/PUT-Endpoints für Stammdaten-Management, GET/POST-Endpoints für Echtzeit-Bestandsanpassungen (R_2.2.3).
- OrderProposalApiController: POST-Endpoint zur Bestellauslöung (Schnellbestellung R_2.4.4A), POST-Endpoint zur Bestellauslöung mit Bearbeitung (R_2.4.4B, inkl. geänderter Menge, Lieferant, Lieferdatum – pass Benutzer-ID konzeptionell vom Frontend R_3.5.1 Audittrail). Diese Endpoints nutzen OrderProposalService/OrderService, die wiederum Audit-Logs schreiben.
- MailApiController (optional): Endpoint zum manuellen Triggern von Alert-Mails zu Testzwecken.

#### F4.2: Frontend (Konzeptionell, z.B. SPA-Komponenten R_3.1.4):

Beschreibung: Visualisierung und Benutzerinteraktion im Dashboard.

Entwickler-Tasks:

- Dashboard-Ansicht:
- Tabelle aller Bestellvorschläge (aus F4.1-DashboardController).
- Highlight kritischer Bestandslevel (R_2.4.2).
- Aktionen-Buttons per Vorschlags-Zeile: "Schnellbestellung", "Bearbeiten".
- Bearbeitungs-Modal/Sektion (R_2.4.3 & R_2.4.4B):
- Interaktives Formular, vorausgefüllt mit Standardwerten für Menge, Lieferant, erwartetes Datum.
- Felder für Bestellmenge, Lieferant, Gewünschtes Lieferdatum bearbeitbar.
- Buttons: "Bestellung Bestätigen" (triggert Modified Order API Call inkl. konzeptioneller User-ID), "Abbrechen".
- Real-Time Bestandsanpassung (R_2.2.3): Einfaches Formular/Ansicht zur manuellen Stock-Korrektur.
- Order History / Audit Trail (R_2.4.5 & R_3.5.4): Konzeptionelle oder explizite Seite zur Ansicht vergangener, manuell ausgelöster Bestellungen mit Benutzer, Zeitstempel, Änderungen und Status.

### Phase 5: Fehlerbehandlung, Alerting & Audit Log (Robustheit R_3.5)

#### F5.1: Exception Handling in Batch & API:

Beschreibung: Konsistente Fehlerbehandlung über die gesamte Anwendung.

Entwickler-Tasks:

- Batch: Robustes Handling von Fehlern beim Datei-Import (retry, skip, listeners, Fehler-Log).
- API: Exception Handler in Controllern für transaktionssichere Antworten bei Fehlern (z.B. gesperrte Datei, DB-Fehler, FK-Fehler).

#### F5.2: E-Mail Alerting (R_3.5.3):

Beschreibung: Benachrichtigung der Administratoren bei kritischen Fehlern.

Entwickler-Tasks:

- Hinzufügen von spring-boot-starter-mail.
- MailAlertService implementieren: Sendet E-Mails bei kritischen Fehlern (z.B. DB-Verbindungsausfall).

#### F5.3: Audit Log (Bestellungen R_3.5.4):

Beschreibung: Transaction-sicheres Logging aller manuellen Bestellaktionen.

Entwickler-Tasks:

- Sicherstellen, dass die OrderService-Methoden zur Bestellauslöung transaktionssicher Audit-Log-Einträge in der bestellungen-Tabelle (oder einer separaten AuditLog-Tabelle) schreiben, inkl. Zeitstempel und der konzeptionell vom Frontend übergebenen Benutzer-ID (R_3.5.1).

