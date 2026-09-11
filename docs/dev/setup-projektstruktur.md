# Setup: Projektstruktur & Konfiguration

**Ziel**: Du verstehst den Aufbau des Projekts, die Maven-Abhängigkeiten und die Spring-Boot-Konfiguration.

## Projektstruktur

```
lager-management/
├── backend/                     # Spring Boot 3 Backend (Java 25)
│   ├── Dockerfile               # Multi-Stage: Maven → JRE-Alpine
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/lagermanagement/space/
│       │   ├── config/          # Spring Security, AppSecurityProperties
│       │   ├── domain/
│       │   │   ├── entity/      # JPA-Entities (Artikel, Lieferant, ...)
│       │   │   ├── repository/  # Spring Data JPA Interfaces
│       │   │   └── enums/       # Status-Enums (ArtikelStatus, VorschlagStatus, ...)
│       │   ├── service/         # Geschäftslogik (@Service)
│       │   │   └── batch/       # @Scheduled Jobs (Import, Reorder-Analyse)
│       │   └── web/
│       │       ├── controller/  # REST-Endpunkte (/api/v1/...)
│       │       ├── dto/         # Request/Response Records & JAXB-Klassen
│       │       └── handler/     # GlobalExceptionHandler
│       └── resources/
│           ├── application.yml  # Zentrale Konfiguration
│           └── schema.sql       # Datenbankschema (wird bei jedem Start ausgeführt)
├── frontend/                    # Vue 3 + Vite Frontend
│   ├── Dockerfile               # Multi-Stage: Node → Nginx-Alpine
│   ├── nginx.conf               # Reverse Proxy + SPA-Fallback
│   ├── vite.config.js
│   └── src/
│       ├── api/
│       │   ├── axios.js         # Axios-Instanz mit Base-URL + Auth-Interceptor
│       │   └── services/        # API-Service-Funktionen (authService, artikelService, ...)
│       ├── stores/              # Pinia Stores (authStore)
│       ├── router/              # Vue Router + Auth-Guard
│       ├── views/               # Seiten-Komponenten (LoginView, DashboardView, ...)
│       └── components/          # Wiederverwendbare Komponenten (AppHeader, BestellModal, ...)
├── files/                       # Datei-Mount (Import-Input + Export-Output)
├── docker-compose.yml
├── .env.example
└── docs/                        # Projektdokumentation
    ├── dev/                     # Entwickler-Dokumentation (API, Setup, Architektur)
    ├── user/                    # Benutzerhandbücher
    ├── plans/                   # Masterplan (Read-Only)
    ├── todo/                    # Ticket-Verwaltung
    └── test/                    # Testergebnisse
```

## Maven-Abhängigkeiten (pom.xml)

Alle relevanten Abhängigkeiten für das Backend:

| Dependency | Version | Zweck |
|---|---|---|
| `spring-boot-starter-web` | 3.4.5 | REST-API |
| `spring-boot-starter-data-jpa` | 3.4.5 | ORM / Spring Data |
| `spring-boot-starter-security` | 3.4.5 | HTTP Basic Auth, Session |
| `spring-boot-starter-validation` | 3.4.5 | Jakarta Bean Validation (`@Valid`) |
| `spring-boot-starter-mail` | 3.4.5 | E-Mail-Versand (optional) |
| `postgresql` | (BOM) | PostgreSQL JDBC-Treiber |
| `lombok` | (BOM) | Code-Reduktion (`@Data`, `@Builder`, ...) |
| `openpdf` | 2.x | PDF-Generierung für Bestellungen |
| `jakarta.xml.bind-api` + `jaxb-impl` | 4.x / 4.x | XML-Serialisierung (Bestellausgabe) |
| `apache-poi` (xlsx) | 5.x | Excel-Import (.xlsx) |
| `opencsv` | 5.x | CSV-Import |
| `commons-codec` | 1.x | SHA-256 Hash (Import-Duplikatschutz) |

## application.yml Konfiguration

Die zentrale Konfigurationsdatei liegt unter `backend/src/main/resources/application.yml`. Sensible Werte kommen ausschließlich aus der `.env`-Datei.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:db}:5432/${DB_NAME:lagerdb}
    username: ${DB_USER:lager}
    password: ${DB_PASSWORD}            # Kein Default – Fail Fast!
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 10

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none                    # Schema nur via schema.sql
    show-sql: false

  sql:
    init:
      mode: always                      # schema.sql bei jedem Start

  servlet:
    multipart:
      max-file-size: 50MB

app:
  security:
    operator-password-hash: ${APP_OPERATOR_PASSWORD_HASH}  # Kein Default – Fail Fast!
  files:
    base-path: ${FILES_BASE_PATH:/app/files}
  reorder:
    consumption-period-days: 30         # Zeitraum für Verbrauchsanalyse
    cron-expression: "0 0 2 * * *"      # Täglich 02:00 Uhr
  import:
    cron-expression: "0 */15 * * * *"   # Alle 15 Minuten
  report:
    cron-expression: "0 30 6 * * *"     # Täglich 06:30 Uhr
  mail:
    enabled: ${MAIL_ENABLED:false}
    from: ${MAIL_FROM:lager@localhost}
```

## Fail-Fast-Mechanismus

Fehlen kritische Secrets beim Start, bricht die Anwendung sofort mit einem klaren Fehler ab. Das verhindert, dass eine falsch konfigurierte Instanz unbemerkt im Betrieb ist.

Implementiert in `AppSecurityProperties.java` (`config/`):

```java
@ConfigurationProperties(prefix = "app.security")
@Validated
public class AppSecurityProperties {
    @NotEmpty
    private String operatorPasswordHash;
}
```

Ist `APP_OPERATOR_PASSWORD_HASH` leer oder nicht gesetzt, bricht der Spring-Start mit `APPLICATION FAILED TO START` ab.

## Enums (domain/enums/)

| Enum | Werte | Verwendung |
|---|---|---|
| `ArtikelStatus` | `AKTIV`, `INAKTIV` | `artikel.status` |
| `BestellungStatus` | `OFFEN`, `ABGESCHLOSSEN`, `STORNIERT` | `bestellungen.status` |
| `VorschlagStatus` | `VORSCHLAG`, `BESTELLT`, `IGNORIERT` | `bestellvorschlaege.status` |
| `TransaktionTyp` | `EINGANG`, `AUSGANG` | `transaktionen.typ` |
| `TransaktionQuelle` | `MANUELL`, `BATCH` | `transaktionen.quelle` |
