# Architektur: Gesamtübersicht

**Ziel**: Du verstehst den Gesamtaufbau des Lager-Management-Systems und das Zusammenspiel aller Komponenten.

## Systemübersicht

```
┌─────────────────────────────────────────────────┐
│               Docker Compose                     │
│                                                  │
│  ┌──────────┐    ┌──────────┐    ┌────────────┐ │
│  │  Browser │    │  Nginx   │    │  Backend   │ │
│  │ (Vue SPA)│◄──►│ :80      │◄──►│ Spring Boot│ │
│  └──────────┘    │ Reverse  │    │ :8080      │ │
│                  │ Proxy    │    └─────┬──────┘ │
│                  └──────────┘          │        │
│                                  ┌────▼──────┐  │
│                                  │PostgreSQL │  │
│                                  │ :5432     │  │
│                                  └───────────┘  │
└─────────────────────────────────────────────────┘
```

Zugriff von außen: **nur Port 80** (Nginx). Backend und Datenbank sind ausschließlich im internen Docker-Netzwerk erreichbar.

## Technologie-Stack

| Schicht | Technologie | Version |
|---|---|---|
| Frontend | Vue 3 + Vite | Vue 3.x |
| UI-Komponenten | PrimeVue | 4.x |
| State Management | Pinia | 2.x |
| HTTP-Client | Axios | 1.x |
| Backend | Spring Boot | 3.4.5 |
| Sprache | Java | 25 |
| Persistenz | Spring Data JPA + Hibernate | 3.4.5 |
| Datenbank | PostgreSQL | 16 |
| Security | Spring Security (HTTP Basic + Session) | 3.4.5 |
| PDF | OpenPDF | 2.x |
| XML | JAXB | 4.x |
| Import (CSV) | OpenCSV | 5.x |
| Import (Excel) | Apache POI | 5.x |
| Webserver / Proxy | Nginx | Alpine |
| Container | Docker Compose | — |

## Backend-Schichten

```
web/controller/        ← REST-Endpunkte (dünn, keine Logik)
      │
service/               ← Geschäftslogik (@Service, @Transactional)
      │
domain/repository/     ← Spring Data JPA Interfaces
      │
domain/entity/         ← JPA-Entities (Lombok @Builder, @Data)
      │
PostgreSQL             ← schema.sql (CREATE TABLE IF NOT EXISTS)
```

**Prinzip**: Entities verlassen die Service-Schicht nie in Richtung Frontend. Controller geben ausschließlich DTOs (Java Records) zurück.

## Frontend-Schichten

```
views/           ← Seiten (je Route eine View)
components/      ← Wiederverwendbare UI-Bausteine (AppHeader, BestellModal)
stores/          ← Globaler State (authStore via Pinia)
api/services/    ← API-Aufrufe (artikelService, dashboardService, ...)
api/axios.js     ← Axios-Instanz: Base-URL /api/v1, credentials, 401-Interceptor
router/          ← Vue Router: Routen mit requiresAuth-Guard
```

## API-Überblick

Alle Endpunkte starten mit `/api/v1/`:

| Ressource | Basis-Pfad | Beschreibung |
|---|---|---|
| Authentifizierung | `/api/v1/auth/` | Login, Logout, Me |
| Artikel | `/api/v1/artikel/` | CRUD + Bestand |
| Lieferanten | `/api/v1/lieferanten/` | Lesen + Aktualisieren |
| Dashboard | `/api/v1/dashboard/` | KPIs + Bestellvorschläge |
| Reorder | `/api/v1/reorder/` | Manueller Analyse-Trigger |
| Bestellungen | `/api/v1/bestellungen/` | Bestellungen aufgeben & abrufen |
| Bestellvorschläge | `/api/v1/bestellvorschlaege/` | Ignorieren |
| Lagerbewegungen | `/api/v1/bewegungen/` | Eingang / Ausgang buchen |
| Transaktionen | `/api/v1/transaktionen/` | Verlauf (paginiert, filterbar) |

## Authentifizierung

HTTP Form Login → `JSESSIONID`-Cookie (HttpOnly). Kein JWT, kein OAuth. Genau ein Benutzer: `operator`.

Alle Requests nach dem Login senden das Cookie automatisch (`withCredentials: true` in Axios). Bei einer `401`-Antwort leitet Axios automatisch auf `/login` um.

## Batch-Jobs

| Job | Frequenz | Aufgabe |
|---|---|---|
| `ImportScheduler` | Alle 15 Min | CSV/Excel-Dateien aus `files/Input/` importieren |
| `ReorderScheduler` | Täglich 02:00 | Reorder-Analyse + neue Bestellvorschläge |
| `ReorderScheduler` | Täglich 06:30 | Täglichen Bestandsbericht als PDF generieren |

## Fehlerbehandlung

Der `GlobalExceptionHandler` (`@RestControllerAdvice`) fängt alle Exceptions ab und liefert immer dasselbe JSON-Format:

```json
{
  "status": 404,
  "message": "Artikel mit ID 42 nicht gefunden",
  "errors": null
}
```

Bei Validierungsfehlern (`400`) enthält `errors` ein Map mit Feldnamen und Fehlermeldungen. Stacktraces, Datenbankdetails und interne Pfade werden nie ans Frontend weitergegeben.

## Audit-Trail

Bei jeder manuell ausgelösten Lagerbewegung und Bestellung liest das System den aktuellen Benutzernamen aus dem `SecurityContextHolder` und speichert ihn in `erstellt_von` (Bestellungen) bzw. `benutzer_id` (Transaktionen).
