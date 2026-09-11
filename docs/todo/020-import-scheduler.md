---
Titel: ImportScheduler – zeitgesteuerter Datei-Import
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `ImportScheduler` als `@Scheduled`-Job, der alle 15 Minuten den `Input/`-Ordner scannt und neue Dateien verarbeitet. Der Cron-Ausdruck muss über `application.yml` konfigurierbar sein.

Referenz: Masterplan Unit 5 — R-B1, Abschnitt "ImportScheduler"

**Zu erstellende Datei:**
- `backend/src/main/java/com/lagermanagement/space/service/batch/ImportScheduler.java`

**Scheduler-Konfiguration:**
```java
@Scheduled(cron = "${app.import.cron-expression}")
public void runImport() {
    stammdatenImportService.processAll();
    transaktionenImportService.processAll();
}
```

**Cron-Ausdruck in `application.yml`:**
```yaml
app:
  import:
    cron-expression: "0 */15 * * * *"  # alle 15 Minuten
```

**Reihenfolge:** Stammdaten-Import läuft vor Transaktionen-Import (FK-Abhängigkeit: Artikel muss existieren, bevor Transaktionen dafür gebucht werden können).

**Fehlerbehandlung:** Der Scheduler darf bei einem Fehler in einem Import-Lauf den nächsten geplanten Lauf NICHT unterbrechen. Fehler werden als ERROR geloggt.

**`@EnableScheduling`** muss in einer `@Configuration`-Klasse (z.B. `SpaceApplication` oder eigene Config) aktiviert sein.

## Akzeptanzkriterien

- [ ] `ImportScheduler` ist mit `@Component` + `@RequiredArgsConstructor` annotiert
- [ ] Cron-Ausdruck kommt aus `${app.import.cron-expression}` (nicht hard-coded)
- [ ] `@EnableScheduling` ist in der Applikation aktiviert
- [ ] Stammdaten-Import läuft vor Transaktionen-Import
- [ ] Fehler in einem Import-Durchlauf unterbrechen den nächsten Scheduler-Lauf NICHT
- [ ] Kein `@Autowired` — ausschließlich Constructor-Injection
---




---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
