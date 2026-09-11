# API: Bestellungen & Bestellvorschläge

Die Bestell-API ermöglicht das Aufgeben von Bestellungen aus vorhandenen Bestellvorschlägen sowie das Abrufen der Bestellhistorie.

---

## POST /api/v1/bestellungen/schnell/{vorschlagId}

**Beschreibung**: Erstellt eine Schnellbestellung aus einem Bestellvorschlag. Alle Werte (Menge, Preis) werden direkt von den Artikelstammdaten übernommen.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `vorschlagId` | `Long` (Path) | Ja | ID des Bestellvorschlags |

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/bestellungen/schnell/10' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
{
  "bestellnummer": "BEST-2026-001",
  "artikelBezeichnung": "Schraube M6",
  "lieferantName": "Metallbau GmbH",
  "bestellmenge": 200,
  "einkaufspreis": 0.12,
  "pdfPfad": "Output/Orders/Bestellung_BEST-2026-001_2026-04-28.pdf"
}
```

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Bestellvorschlag nicht gefunden |
| `422` | Vorschlag ist nicht im Status `VORSCHLAG` |

---

## POST /api/v1/bestellungen/bearbeitet/{vorschlagId}

**Beschreibung**: Erstellt eine Bestellung aus einem Vorschlag mit abweichenden, manuell eingegebenen Werten (Menge, Preis, Lieferdatum, Notiz).  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `vorschlagId` | `Long` (Path) | Ja | ID des Bestellvorschlags |

### Request-Body

```json
{
  "bestellmenge": 300,
  "einkaufspreis": 0.10,
  "gewuenschtesLieferdatum": "2026-05-05",
  "notiz": "Sonderkonditionen vereinbart"
}
```

### Validierungsregeln

| Feld | Pflicht | Regel |
|---|---|---|
| `bestellmenge` | Ja | ≥ 1 |
| `einkaufspreis` | Nein | > 0, falls angegeben |
| `gewuenschtesLieferdatum` | Nein | ISO-Datumsformat (`YYYY-MM-DD`) |
| `notiz` | Nein | Freitext |

### Response (200 OK)

Gleiche Struktur wie Schnellbestellung-Response.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler |
| `404` | Bestellvorschlag nicht gefunden |
| `422` | Vorschlag ist nicht im Status `VORSCHLAG` |

---

## PUT /api/v1/bestellvorschlaege/{vorschlagId}/ignorieren

**Beschreibung**: Markiert einen Bestellvorschlag als ignoriert (Status `IGNORIERT`). Der Vorschlag erscheint nicht mehr im Dashboard. Fällt der Bestand unter den Sicherheitsbestand, erstellt die nächste Reorder-Analyse automatisch einen neuen Vorschlag.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `vorschlagId` | `Long` (Path) | Ja | ID des Bestellvorschlags |

### Request-Beispiel

```bash
curl -X PUT 'http://localhost/api/v1/bestellvorschlaege/10/ignorieren' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Kein Body.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Bestellvorschlag nicht gefunden |
| `422` | Vorschlag ist nicht im Status `VORSCHLAG` |

---

## GET /api/v1/bestellungen

**Beschreibung**: Gibt alle Bestellungen zurück (sortiert nach Erstelldatum, neueste zuerst).  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/bestellungen' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
[
  {
    "id": 1,
    "bestellnummer": "BEST-2026-001",
    "artikelId": 1,
    "artikelnummer": "ART-001",
    "lieferantName": "Metallbau GmbH",
    "bestellmenge": 200,
    "einkaufspreis": 0.12,
    "gewuenschtesLieferdatum": "2026-05-05",
    "notiz": null,
    "status": "OFFEN",
    "erstelltVon": "operator",
    "erstelltAm": "2026-04-28T10:30:00"
  }
]
```

---

## GET /api/v1/bestellungen/{id}

**Beschreibung**: Gibt eine einzelne Bestellung anhand der internen ID zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Bestellungs-ID |

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Bestellung nicht gefunden |
