---
Titel: GlobalExceptionHandler + ApiErrorDto + Custom Exceptions
Status: [DONE]

## Implementierungsnotiz
Implementierung abgeschlossen. `ApiErrorDto` als Java Record mit Factory-Methoden `of()`. `EntityNotFoundException` und `BusinessException` als RuntimeException-Subklassen. `GlobalExceptionHandler` behandelt 5 Exception-Typen (400/404/409/422/500). Fallback 500 gibt generische Meldung — kein Stacktrace, keine DB-Details. Tests in `GlobalExceptionHandlerTest` mit 6 Unit-Tests inkl. Sicherheitscheck auf kein Datenleak.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den zentralen `GlobalExceptionHandler` als `@RestControllerAdvice`, das einheitliche `ApiErrorDto` und zwei Custom-Exception-Klassen. Alle REST-Fehler müssen ein konsistentes JSON-Format liefern — niemals Stacktraces, Tabellennamen oder interne DB-Fehler.

Referenz: Masterplan Unit 4 — R_3.5.4, Data Leakage Sicherheitsregel

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/handler/GlobalExceptionHandler.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/ApiErrorDto.java`
- `backend/src/main/java/com/lagermanagement/space/exception/EntityNotFoundException.java`
- `backend/src/main/java/com/lagermanagement/space/exception/BusinessException.java`

**ApiErrorDto (Java Record):**
```java
public record ApiErrorDto(int status, String message, LocalDateTime timestamp, Object details) {}
```

**GlobalExceptionHandler — behandelte Exceptions:**

| Exception | HTTP-Status | Beschreibung |
|---|---|---|
| `MethodArgumentNotValidException` | `400 Bad Request` | Validierungsfehler mit Feldfehlerliste |
| `EntityNotFoundException` | `404 Not Found` | Entity nicht gefunden |
| `DataIntegrityViolationException` | `409 Conflict` | DB-Constraint-Verletzung (z.B. doppelte Artikelnummer) |
| `BusinessException` | `422 Unprocessable Entity` | Fachlicher Fehler (z.B. kein offene Bestellung) |
| `Exception` (Fallback) | `500 Internal Server Error` | Generischer Fehler — NUR generische Meldung, KEIN Stacktrace |

**Custom Exceptions:**
- `EntityNotFoundException extends RuntimeException`
- `BusinessException extends RuntimeException`

**Sicherheitsregel:** Der `GlobalExceptionHandler` darf dem Frontend NIEMALS Stacktraces, interne DB-Fehlermeldungen, Tabellennamen oder absolute Dateipfade zurückgeben.

## Akzeptanzkriterien

- [ ] `GlobalExceptionHandler` ist mit `@RestControllerAdvice` annotiert
- [ ] `ApiErrorDto` ist ein Java Record mit `status`, `message`, `timestamp`, `details`
- [ ] Alle 5 Exception-Handler-Methoden sind implementiert
- [ ] `MethodArgumentNotValidException` gibt Feldfehlerliste zurück (nicht nur generische Meldung)
- [ ] Fallback-Handler `Exception` gibt KEINE Stack-Trace-Details zurück
- [ ] `EntityNotFoundException` und `BusinessException` sind als `RuntimeException`-Subklassen angelegt
- [ ] `DataIntegrityViolationException` gibt `409` zurück — ohne interne DB-Details
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-uebersicht.md
