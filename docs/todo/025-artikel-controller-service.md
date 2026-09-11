---
Titel: ArtikelController + ArtikelService + Artikel-DTOs
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den vollständigen Artikel-CRUD via `ArtikelController` und `ArtikelService`. Artikel können über die UI angelegt und bearbeitet werden; physisches Löschen ist verboten — nur Deaktivieren (`INAKTIV`).

Referenz: Masterplan Unit 7 — R-N1, R-N2, R-N3, R-N4, R_2.4.1, R_2.4.2

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/web/controller/ArtikelController.java`
- `backend/src/main/java/com/lagermanagement/space/service/ArtikelService.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/ArtikelDto.java` (Java Record)
- `backend/src/main/java/com/lagermanagement/space/web/dto/ArtikelCreateRequest.java` (Java Record mit `@Valid`)
- `backend/src/main/java/com/lagermanagement/space/web/dto/ArtikelUpdateRequest.java` (Java Record mit `@Valid`)
- `backend/src/test/java/com/lagermanagement/space/web/controller/ArtikelControllerTest.java`

**Endpunkte:**

| Methode | Pfad | Status | Beschreibung |
|---|---|---|---|
| `GET` | `/api/v1/artikel` | 200 | Alle Artikel (aktiv + inaktiv) |
| `GET` | `/api/v1/artikel/{id}` | 200 / 404 | Einzelner Artikel |
| `POST` | `/api/v1/artikel` | 201 / 400 / 409 | Neuen Artikel anlegen |
| `PUT` | `/api/v1/artikel/{id}` | 200 / 404 | Artikel bearbeiten |
| `PUT` | `/api/v1/artikel/{id}/deaktivieren` | 200 / 404 | Status → INAKTIV |
| `GET` | `/api/v1/artikel/{id}/bestand` | 200 / 404 | Aktuellen Bestand abrufen |

**ArtikelDto (Record):**
```java
public record ArtikelDto(Long id, String artikelnummer, String bezeichnung,
    String mengeneinheit, String warengruppe, Long lieferantId, String lieferantName,
    int aktuellerBestand, int sicherheitsbestand, int bestellpunkt,
    int standardBestellmenge, BigDecimal einkaufspreis, ArtikelStatus status) {}
```

**Validierung:**
- `POST /api/v1/artikel`: Prüft `artikelnummer`-Eindeutigkeit → `409 Conflict` wenn bereits vorhanden
- `@Valid` auf Request-Body, Jakarta Bean Validation auf Pflichtfeldern

## Akzeptanzkriterien

- [ ] Alle 6 Endpunkte sind implementiert
- [ ] `POST /api/v1/artikel` mit doppelter `artikelnummer` gibt `409` zurück
- [ ] `POST /api/v1/artikel` ohne Pflichtfelder gibt `400` mit Feldfehlerliste zurück
- [ ] `PUT /api/v1/artikel/{id}/deaktivieren` setzt Status auf `INAKTIV` (kein physisches Löschen)
- [ ] `artikelnummer` ist nach Anlage nicht mehr per PUT änderbar
- [ ] Keine Entities im Response — ausschließlich `ArtikelDto`
- [ ] `ArtikelControllerTest` mit `@WebMvcTest` + gemocktem Service ist grün
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-artikel.md
