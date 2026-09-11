# API: Lagerbewegungen

Die Bewegungs-API bucht manuelle Wareneingänge und -ausgänge und aktualisiert den Lagerbestand atomar.

---

## POST /api/v1/bewegungen/eingang

**Beschreibung**: Bucht einen manuellen Wareneingang. Der `aktueller_bestand` des Artikels wird erhöht. Der Benutzer aus der aktiven Session wird als `benutzer_id` im Audit-Trail gespeichert.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Body

```json
{
  "artikelId": 1,
  "menge": 200,
  "datum": "2026-04-28"
}
```

### Validierungsregeln

| Feld | Pflicht | Regel |
|---|---|---|
| `artikelId` | Ja | Muss existieren |
| `menge` | Ja | ≥ 1 |
| `datum` | Ja | ISO-Datumsformat (`YYYY-MM-DD`) |

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/bewegungen/eingang' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE' \
  -H 'Content-Type: application/json' \
  -d '{"artikelId": 1, "menge": 200, "datum": "2026-04-28"}'
```

### Response (200 OK)

```json
{
  "transaktionId": 42,
  "bestellungKannGeschlossenWerden": true
}
```

| Feld | Typ | Beschreibung |
|---|---|---|
| `transaktionId` | `Long` | ID der erstellten Transaktion |
| `bestellungKannGeschlossenWerden` | `boolean` | `true`, wenn eine offene Bestellung für diesen Artikel vorhanden ist, die jetzt abgeschlossen werden kann |

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler |
| `404` | Artikel nicht gefunden |

---

## POST /api/v1/bewegungen/eingang/{transaktionId}/bestellung-abschliessen

**Beschreibung**: Schließt die zugehörige offene Bestellung eines Wareneingangs ab (Status → `ABGESCHLOSSEN`). Wird aufgerufen, wenn der Wareneingang einer offenen Bestellung zugeordnet werden soll.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `transaktionId` | `Long` (Path) | Ja | ID der Eingangs-Transaktion |

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/bewegungen/eingang/42/bestellung-abschliessen' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Kein Body.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Transaktion nicht gefunden |
| `422` | Keine offene Bestellung für diesen Artikel vorhanden |

---

## POST /api/v1/bewegungen/ausgang

**Beschreibung**: Bucht einen manuellen Warenausgang. Der `aktueller_bestand` des Artikels wird reduziert. Der Benutzer aus der aktiven Session wird als `benutzer_id` gespeichert.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Body

```json
{
  "artikelId": 1,
  "menge": 50,
  "datum": "2026-04-28",
  "buchungstyp": "VERBRAUCH",
  "grund": "Produktionslinie A"
}
```

### Validierungsregeln

| Feld | Pflicht | Regel |
|---|---|---|
| `artikelId` | Ja | Muss existieren |
| `menge` | Ja | ≥ 1 |
| `datum` | Ja | ISO-Datumsformat (`YYYY-MM-DD`) |
| `buchungstyp` | Ja | Freitext, z.B. `VERBRAUCH`, `ABSCHREIBUNG`, `RETOURE` |
| `grund` | Nein | Freitext |

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/bewegungen/ausgang' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE' \
  -H 'Content-Type: application/json' \
  -d '{"artikelId": 1, "menge": 50, "datum": "2026-04-28", "buchungstyp": "VERBRAUCH"}'
```

### Response (200 OK)

Kein Body.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler |
| `404` | Artikel nicht gefunden |
| `422` | Unzureichender Bestand (Bestand würde negativ werden) |

## Hinweis: Parallelitätsschutz

Der `InventoryService` setzt `@Lock(PESSIMISTIC_WRITE)` beim Lesen des Artikels für Bestandsänderungen. Das verhindert Race-Conditions bei gleichzeitigen manuellen Buchungen und laufenden Batch-Importen.
