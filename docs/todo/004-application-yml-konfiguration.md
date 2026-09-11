---
Titel: application.yml erstellen & application.properties ablösen
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die zentrale `application.yml` für das Spring Boot Backend. Die vorhandene `application.properties` wird gelöscht und durch `application.yml` ersetzt. Alle sensiblen Werte kommen ausschließlich über Umgebungsvariablen (Platzhalter wie `${DB_PASSWORD}`), nie als Klartext.

Referenz: Masterplan Unit 2 — R-D1, R-D2, R-A1

## Akzeptanzkriterien

- [x] `application.yml` vollständig konfiguriert (sauber strukturiert, kein doppelter Block)
- [x] `application.properties` nicht vorhanden (nur `application.yml`)
- [x] `spring.jpa.hibernate.ddl-auto: none` gesetzt
- [x] `spring.sql.init.mode: always` gesetzt
- [x] `DB_PASSWORD` und `APP_OPERATOR_PASSWORD_HASH` ohne Default (YAML-seitig korrekt)
- [x] MAIL-Konfiguration korrekt unter `spring.mail.*` und `app.mail.*` getrennt
- [x] Hikari-Connection-Pool konfiguriert (connection-timeout, maximum-pool-size)
- [x] `spring.jpa.open-in-view: false` gesetzt (WARN-002 behoben, 2026-04-20)
- [x] Fail Fast für `APP_OPERATOR_PASSWORD_HASH`: `AppSecurityProperties` mit `@Validated`+`@NotEmpty` implementiert (2026-04-20)
- [x] Starttest mit fehlenden Secrets vollständig bestätigt — `APP_OPERATOR_PASSWORD_HASH=""` ? APPLICATION FAILED TO START (bestätigt 2026-04-21)

## Notizen

? **WARN-001 behoben**: `AppSecurityProperties.java` in `config/` angelegt.
`@ConfigurationProperties(prefix="app.security")` + `@Validated` + `@NotEmpty` auf `operatorPasswordHash`.
`@ConfigurationPropertiesScan` in `SpaceApplication.java` ergänzt.
Spring Boot schlägt jetzt beim Start fehl, wenn `APP_OPERATOR_PASSWORD_HASH` leer oder nicht gesetzt ist.

? **WARN-002 behoben**: `spring.jpa.open-in-view: false` in `application.yml` ergänzt.

Implementierung abgeschlossen. Tester muss Fail-Fast-Integrationstest im Container wiederholen.
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-projektstruktur.md
