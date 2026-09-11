# Testergebnis: Domain-Enums (006) + application.yml Fail-Fast (004)

**Datum**: 2026-04-21  
**Tickets**: `docs/todo/006-domain-enums.md`, `docs/todo/004-application-yml-konfiguration.md`  
**Tester**: kev-tester  
**Gesamt-Status**: ✅ GRÜN — Alle Tests bestanden

---

## Getestete Klassen / Komponenten

| Datei | Testmethode |
|---|---|
| `domain/enums/ArtikelStatus.java` | JUnit 5 Unit-Tests (`DomainEnumsTest`) |
| `domain/enums/VorschlagStatus.java` | JUnit 5 Unit-Tests (`DomainEnumsTest`) |
| `domain/enums/TransaktionTyp.java` | JUnit 5 Unit-Tests (`DomainEnumsTest`) |
| `domain/enums/TransaktionQuelle.java` | JUnit 5 Unit-Tests (`DomainEnumsTest`) |
| `config/AppSecurityProperties.java` | Docker-Integrationstest (Fail-Fast mit leerem Hash) |

---

## Ticket 006 — Domain-Enums

**Testbefehl**: `mvn test -Dtest=DomainEnumsTest`  
**Ergebnis**: `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`  
**Dauer**: ~10s (Build: 15s gesamt)

### Coverage-Übersicht

| Klasse | Getestete Aspekte | Ergebnis |
|--------|------------------|----------|
| `ArtikelStatus` | Alle Werte (AKTIV, INAKTIV), Schema-Konsistenz DEFAULT 'AKTIV' | ✅ |
| `VorschlagStatus` | Alle 4 Werte (VORSCHLAG, BESTELLT, GELIEFERT, IGNORIERT), Schema-Konsistenz | ✅ |
| `TransaktionTyp` | Alle Werte (EINGANG, AUSGANG) | ✅ |
| `TransaktionQuelle` | Alle Werte (BATCH, MANUELL), Schema-Konsistenz DEFAULT 'BATCH' | ✅ |

### Ausgeführte Tests

| Testmethode | Beschreibung | Ergebnis |
|---|---|---|
| `shouldContainAllArtikelStatusValues` | Prüft AKTIV + INAKTIV | ✅ |
| `shouldContainAllVorschlagStatusValues` | Prüft VORSCHLAG, BESTELLT, GELIEFERT, IGNORIERT | ✅ |
| `shouldContainAllTransaktionTypValues` | Prüft EINGANG + AUSGANG | ✅ |
| `shouldContainAllTransaktionQuelleValues` | Prüft BATCH + MANUELL | ✅ |
| `shouldMatchSchemaDefaultForArtikelStatus` | `AKTIV.name() == "AKTIV"` | ✅ |
| `shouldMatchSchemaDefaultForVorschlagStatus` | `VORSCHLAG.name() == "VORSCHLAG"` | ✅ |
| `shouldMatchSchemaDefaultForTransaktionQuelle` | `BATCH.name() == "BATCH"` | ✅ |

---

## Ticket 004 — application.yml Fail-Fast (APP_OPERATOR_PASSWORD_HASH)

**Testmethode**: Docker-Integrationstest — Container-Start mit `APP_OPERATOR_PASSWORD_HASH=""`  
**Befehl**:
```bash
docker compose run --rm -e APP_OPERATOR_PASSWORD_HASH="" backend
```

**Ergebnis**: Container startet NICHT. Exit-Code: 1.

**Tatsächliche Fehlerausgabe (Nachweis)**:
```
WARN  ConfigServletWebServerApplicationContext : Exception encountered during context initialization
ERROR o.s.b.d.LoggingFailureAnalysisReporter  : APPLICATION FAILED TO START

Binding to target AppSecurityProperties failed:
    Reason: APP_OPERATOR_PASSWORD_HASH darf nicht leer oder nicht gesetzt sein
```

**Bewertung**:  
`AppSecurityProperties` mit `@Validated` + `@NotEmpty` funktioniert korrekt. Der Mechanismus:
- Spring Boot bindet `app.security.operator-password-hash` aus `application.yml` (`${APP_OPERATOR_PASSWORD_HASH}`)
- Leerer String → `@NotEmpty` schlägt fehl → `ConfigurationPropertiesBindException`
- Kein Container-Start möglich → Fail Fast erfüllt ✅

### Offene Prüfung aus dem Vortest (WARN-001)

| Prüfung | Vortest (2026-04-20) | Dieser Test (2026-04-21) |
|---|---|---|
| Fail Fast bei leerem `APP_OPERATOR_PASSWORD_HASH` | ⚠️ OFFEN | ✅ BESTÄTIGT |

---

## Gefundene Fehler

> Keine. Alle Prüfungen bestanden.

---

## Nächste Schritte

- ✅ **Ticket 006** → Status auf `[REVIEW]` gesetzt.
- ✅ **Ticket 004** → Status auf `[REVIEW]` gesetzt. Letztes offenes Kriterium (Fail-Fast-Integrationstest) bestätigt.
