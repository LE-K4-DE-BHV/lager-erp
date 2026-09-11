# Testergebnis: GlobalExceptionHandler + ApiErrorDto + Custom Exceptions

**Datum**: 2026-04-23
**Ticket**: docs/todo/015-global-exception-handler.md
**Status**: ✅ GRÜN

## Getestete Klassen / Komponenten

- `GlobalExceptionHandler.java` — alle 5 Exception-Handler
- `ApiErrorDto.java` — Java Record, Factory-Methoden `of()`
- `EntityNotFoundException.java` — RuntimeException-Subklasse
- `BusinessException.java` — RuntimeException-Subklasse

## Testdatei

`backend/src/test/java/com/lagermanagement/space/web/handler/GlobalExceptionHandlerTest.java` (vom Dev erstellt)

## Coverage-Übersicht

| Klasse                    | Getestete Pfade                              | Line Coverage (geschätzt) |
|---------------------------|----------------------------------------------|---------------------------|
| `GlobalExceptionHandler`  | alle 5 Handler-Methoden                      | ~100 %                    |
| `ApiErrorDto`             | `of(status, msg)`, `of(status, msg, details)` | ~100 %                   |
| `EntityNotFoundException` | Konstruktor, `getMessage()`                  | 100 %                     |
| `BusinessException`       | Konstruktor, `getMessage()`                  | 100 %                     |

## Testfälle (6/6 grün)

| Test                                                         | Beschreibung                                                                          | Ergebnis |
|--------------------------------------------------------------|---------------------------------------------------------------------------------------|----------|
| `shouldReturn404WhenEntityNotFound`                          | HTTP 404 + korrekter Status-Code + Meldung im Body                                    | ✅ GRÜN  |
| `shouldReturn422WhenBusinessException`                       | HTTP 422 + korrekte Fehlermeldung                                                     | ✅ GRÜN  |
| `shouldReturn409WhenDataIntegrityViolation`                  | HTTP 409 + generische Meldung ohne DB-interne Details (Sicherheitscheck)              | ✅ GRÜN  |
| `shouldReturn500WithGenericMessageWhenUnhandledExceptionOccurs` | HTTP 500 + generische Meldung, kein Stacktrace, keine DB-Details im Body          | ✅ GRÜN  |
| `shouldReturn400WithFieldErrorsWhenValidationFails`          | HTTP 400 + Feldfehlerliste (`bezeichnung`, `menge`) als `details`-Map                 | ✅ GRÜN  |
| `shouldContainTimestampInEveryErrorResponse`                 | Alle Response-Bodies enthalten ein `timestamp`-Feld                                  | ✅ GRÜN  |

## Sicherheitscheck ✅

Die folgenden Sicherheitsregeln wurden explizit getestet und bestehen:

- `shouldReturn409WhenDataIntegrityViolation`: Response enthält **nicht** den internen Exception-Text (`"artikelnummer"`, `"duplicate key"`)
- `shouldReturn500WithGenericMessageWhenUnhandledExceptionOccurs`: Response enthält **nicht** Datenbankverbindungsdetails oder Tabellennamen; `details` ist `null`

## Gefundene Fehler

> (keine)

## Nächste Schritte

- ✅ Ticket auf [REVIEW] gesetzt.
