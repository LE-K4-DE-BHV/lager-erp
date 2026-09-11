---
Titel: ReorderScheduler – nächtlicher Reorder-Job & Berichts-Job
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `ReorderScheduler` mit zwei `@Scheduled`-Methoden: einer für den nächtlichen Reorder-Analyse-Job und einer für den täglichen Bestandsbericht. Beide Cron-Ausdrücke müssen über `application.yml` konfigurierbar sein.

Referenz: Masterplan Unit 6 — R_2.3.2, R-P4, R-P5, Abschnitt "ReorderScheduler"

**Zu erstellende Datei:**
- `backend/src/main/java/com/lagermanagement/space/service/batch/ReorderScheduler.java`

**Scheduler-Methoden:**
```java
@Scheduled(cron = "${app.reorder.cron-expression}")
public void scheduledReorder() {
    reorderAnalysisService.runAnalysis();
}

@Scheduled(cron = "${app.report.cron-expression}")
public void scheduledReport() {
    berichtService.generateDailyReport();
}
```

**Cron-Ausdrücke in `application.yml`:**
```yaml
app:
  reorder:
    cron-expression: "0 0 2 * * *"   # täglich 02:00 Uhr
  report:
    cron-expression: "0 30 6 * * *"  # täglich 06:30 Uhr
```

**Hinweis:** `ReorderAnalysisService.runAnalysis()` ist synchron und kann auch direkt über `POST /api/v1/reorder/trigger` aufgerufen werden (manueller Trigger — R-P4).

## Akzeptanzkriterien

- [ ] `ReorderScheduler` hat zwei `@Scheduled`-Methoden (Reorder + Bericht)
- [ ] Beide Cron-Ausdrücke kommen aus `${app.reorder.cron-expression}` bzw. `${app.report.cron-expression}`
- [ ] `@Component` + `@RequiredArgsConstructor` — kein `@Autowired`
- [ ] Fehler in einem Job-Lauf unterbrechen den nächsten Lauf NICHT
---




---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-reorder.md
