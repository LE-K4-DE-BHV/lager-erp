# Architektur: Sicherheit & Authentifizierung

**Ziel**: Du verstehst, wie Spring Security das System absichert und wie die Authentifizierung funktioniert.

## Überblick

Das System nutzt **Spring Security** mit **HTTP Form Login** und **session-basierter Authentifizierung**. Es gibt genau einen Benutzer: `operator`. Kein JWT, kein OAuth, keine komplexen Rollen.

## Authentifizierungsfluss

```
Browser/Frontend          Nginx             Backend (Spring Security)
     │                      │                        │
     │── POST /api/v1/auth/login ────────────────────▶│
     │   (username + password, form-encoded)           │
     │                      │           formLogin-Filter verarbeitet
     │                      │           Credentials prüfen (BCrypt)
     │◀─── 200 OK + Set-Cookie: JSESSIONID ───────────│
     │     { "username": "operator" }                  │
     │                                                 │
     │── GET /api/v1/... + Cookie: JSESSIONID ────────▶│
     │                      │           Session validieren
     │◀─── 200 OK + Response-Body ────────────────────│
```

Der Login-Endpunkt (`POST /api/v1/auth/login`) wird vollständig vom Spring-Security-`formLogin`-Filter abgefangen und erreicht den `AuthController` nicht.

## Session-Management

- Session-Typ: **Stateful** via `JSESSIONID`-Cookie
- Cookie-Flags: `HttpOnly=true` (kein JavaScript-Zugriff → XSS-Schutz)
- Session-Erstellung: `IF_REQUIRED` (Spring erstellt eine Session nur bei erfolgreichem Login)
- CSRF: **Deaktiviert** (REST-API + Same-Origin via Nginx-Proxy)

## Passwort-Hashing

Passwörter werden ausschließlich als **BCrypt-Hash** gespeichert. Der Hash kommt aus der Umgebungsvariable `APP_OPERATOR_PASSWORD_HASH` und wird nie im Code oder in der Datenbank abgelegt.

```java
@Bean
public UserDetailsService userDetailsService() {
    var operator = User.builder()
            .username("operator")
            .password(securityProperties.getOperatorPasswordHash())  // aus .env
            .roles("OPERATOR")
            .build();
    return new InMemoryUserDetailsManager(operator);
}
```

## Endpunkt-Absicherung

| Endpunkt | Zugang |
|---|---|
| `POST /api/v1/auth/login` | Öffentlich (`permitAll`) |
| Alle anderen `/api/v1/**` | Nur mit gültiger Session |

## Fehler-Responses (JSON)

Spring Security gibt bei Authentifizierungsfehlern **kein HTML** zurück, sondern immer JSON:

**Login fehlgeschlagen (401)**:
```json
{ "status": 401, "message": "Ungültige Anmeldedaten" }
```

**Nicht authentifiziert (401)**:
```json
{ "status": 401, "message": "Nicht authentifiziert" }
```

## Fail-Fast beim Start

Fehlt `APP_OPERATOR_PASSWORD_HASH` in der `.env`, bricht die Anwendung beim Start ab:

```
APPLICATION FAILED TO START
Description: Binding to target ... failed:
  Property: app.security.operatorPasswordHash
  Value: ""
  Reason: darf nicht leer sein
```

Implementiert über `AppSecurityProperties` mit `@ConfigurationProperties` + `@Validated` + `@NotEmpty`.

## CORS

Das System benötigt **keine CORS-Konfiguration**, da Nginx als Reverse Proxy für `/api/`-Anfragen fungiert. Frontend und Backend teilen die gleiche Origin (`http://localhost`).

## Security-Header

Spring Boot setzt grundlegende Security-Header automatisch:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Cache-Control: no-cache, no-store`
