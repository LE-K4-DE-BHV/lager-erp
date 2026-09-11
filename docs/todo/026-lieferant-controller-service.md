---
Titel: LieferantController + LieferantService + Lieferant-DTOs
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `LieferantController` und `LieferantService` für die Lieferantenverwaltung. Lieferanten können nur per Batch-Import angelegt werden — die UI erlaubt ausschließlich das Bearbeiten bestehender Lieferanten (R-V2).

Referenz: Masterplan Unit 7 — R-V1, R-V2

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/controller/LieferantController.java`
- `backend/src/main/java/com/lagermanagement/space/service/LieferantService.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/LieferantDto.java` (Java Record)
- `backend/src/main/java/com/lagermanagement/space/web/dto/LieferantUpdateRequest.java` (Java Record mit `@Valid`)

**Endpunkte:**

| Methode | Pfad | Status | Beschreibung |
|---|---|---|---|
| `GET` | `/api/v1/lieferanten` | 200 | Alle Lieferanten |
| `GET` | `/api/v1/lieferanten/{id}` | 200 / 404 | Einzelner Lieferant |
| `PUT` | `/api/v1/lieferanten/{id}` | 200 / 404 | Lieferant bearbeiten (Name, Kontakt, Lead Time) |

**LieferantDto (Record):**
```java
public record LieferantDto(Long id, String lieferantId, String name,
    String kontaktEmail, String kontaktTelefon, int leadTimeTage) {}
```

**Kein `POST /api/v1/lieferanten`!** Anlegen nur via Batch-Import. Der Controller darf keinen Create-Endpunkt exponieren.

**Editierbare Felder bei PUT:** `name`, `kontaktEmail`, `kontaktTelefon`, `leadTimeTage`
**Nicht editierbar:** `lieferantId` (die externe Geschäfts-ID ist nach Anlage unveränderlich)

## Akzeptanzkriterien

- [ ] `GET /api/v1/lieferanten` gibt alle Lieferanten als `List<LieferantDto>` zurück
- [ ] `PUT /api/v1/lieferanten/{id}` bearbeitet nur erlaubte Felder
- [ ] `lieferantId` (externe ID) ist nicht per PUT änderbar
- [ ] **Kein** `POST /api/v1/lieferanten` Endpunkt vorhanden (Anlage nur via Batch)
- [ ] `GET /api/v1/lieferanten/{unbekannte-id}` gibt `404` zurück
- [ ] Keine Entities im Response — ausschließlich `LieferantDto`
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-lieferant.md
