# API: Dashboard & KPIs

Die Dashboard-API liefert Schlüsselkennzahlen (KPIs) und offene Bestellvorschläge für die Startseite der Anwendung.

---

## GET /api/v1/dashboard/kpis

**Beschreibung**: Gibt die aktuellen Lagerkennzahlen zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/dashboard/kpis' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
{
  "offeneVorschlaege": 5,
  "kritischeVorschlaege": 2,
  "letzterReorderLauf": "2026-04-28T02:00:00"
}
```

| Feld | Typ | Beschreibung |
|---|---|---|
| `offeneVorschlaege` | `int` | Anzahl Vorschläge mit Status `VORSCHLAG` |
| `kritischeVorschlaege` | `int` | Anzahl Vorschläge mit Bestand ≤ Sicherheitsbestand |
| `letzterReorderLauf` | `LocalDateTime` (nullable) | Zeitpunkt der letzten Reorder-Analyse (In-Memory, `null` nach Neustart) |

---

## GET /api/v1/dashboard/vorschlaege

**Beschreibung**: Gibt alle offenen Bestellvorschläge (Status `VORSCHLAG`) zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/dashboard/vorschlaege' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
[
  {
    "id": 10,
    "artikelId": 1,
    "artikelnummer": "ART-001",
    "artikelBezeichnung": "Schraube M6",
    "lieferantName": "Metallbau GmbH",
    "aktuellerBestand": 45,
    "bestellpunkt": 80,
    "sicherheitsbestand": 50,
    "vorgeschlageneMenge": 200,
    "status": "VORSCHLAG",
    "erstelltAm": "2026-04-28T02:00:00",
    "istKritisch": true
  }
]
```

| Feld | Typ | Beschreibung |
|---|---|---|
| `istKritisch` | `boolean` | `true` wenn `aktuellerBestand ≤ sicherheitsbestand` |

---

## POST /api/v1/reorder/trigger

**Beschreibung**: Startet die Reorder-Analyse manuell (außerhalb des geplanten Nachtlaufs).  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/reorder/trigger' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
{
  "message": "Analyse abgeschlossen",
  "neueVorschlaege": 3
}
```

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `401` | Keine gültige Session |
