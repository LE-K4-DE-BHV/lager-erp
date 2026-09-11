---
Titel: DashboardController + KPI-Endpunkte + Reorder-Trigger
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `DashboardController` mit den drei Dashboard-Endpunkten: KPI-Summary, Bestellvorschlagsliste und manueller Reorder-Trigger. Der Controller ist dünn und delegiert jede Logik an den Service-Layer.

Referenz: Masterplan Unit 7 — R-P1, R-P2, R-P3, R-P4

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/controller/DashboardController.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/KpiDto.java` (Java Record)
- `backend/src/main/java/com/lagermanagement/space/web/dto/BestellvorschlagDto.java` (Java Record)

**Endpunkte:**

| Methode | Pfad | Beschreibung |
|---|---|---|
| `GET` | `/api/v1/dashboard/kpis` | KPI-Summary: offene Vorschläge, kritische Vorschläge, letzter Reorder-Lauf |
| `GET` | `/api/v1/dashboard/vorschlaege` | Alle Vorschläge mit Status `VORSCHLAG` oder `BESTELLT` |
| `POST` | `/api/v1/reorder/trigger` | Startet Reorder-Analyse synchron, gibt Ergebnis zurück |

**KpiDto (Record):**
```java
public record KpiDto(int offeneVorschlaege, int kritischeVorschlaege, LocalDateTime letzterReorderLauf) {}
```

**BestellvorschlagDto (Record):**
```java
public record BestellvorschlagDto(Long id, Long artikelId, String artikelnummer,
    String artikelBezeichnung, String lieferantName, int aktuellerBestand,
    int bestellpunkt, int sicherheitsbestand, int vorgeschlageneMenge,
    VorschlagStatus status, LocalDateTime erstelltAm, boolean istKritisch) {}
```

**Reorder-Trigger-Response:** `{ "message": "Analyse abgeschlossen", "neueVorschlaege": 3 }`

## Akzeptanzkriterien

- [ ] `GET /api/v1/dashboard/kpis` gibt korrekte Zählwerte zurück
- [ ] `GET /api/v1/dashboard/vorschlaege` gibt nur Vorschläge mit Status `VORSCHLAG` oder `BESTELLT` zurück
- [ ] `BestellvorschlagDto.istKritisch` ist `true` wenn `aktuellerBestand <= sicherheitsbestand`
- [ ] `POST /api/v1/reorder/trigger` ruft `ReorderAnalysisService.runAnalysis()` auf und gibt Ergebnis zurück
- [ ] Controller hat keinerlei Geschäftslogik — alles wird an Services delegiert
- [ ] Alle Endpunkte erfordern eine aktive Session (Spring Security)
- [ ] Keine Entities im Response — ausschließlich DTOs
---




---
**Doku erstellt (2026-04-28)**: docs/dev/api-dashboard.md
