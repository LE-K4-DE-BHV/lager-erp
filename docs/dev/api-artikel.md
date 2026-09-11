# API: Artikel

Die Artikel-API verwaltet Artikelstammdaten inklusive Bestand und Status.

---

## GET /api/v1/artikel

**Beschreibung**: Gibt alle Artikel zurück (aktive und inaktive).  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/artikel' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
[
  {
    "id": 1,
    "artikelnummer": "ART-001",
    "bezeichnung": "Schraube M6",
    "mengeneinheit": "Stk",
    "warengruppe": "Befestigungsmaterial",
    "lieferantId": 2,
    "lieferantName": "Metallbau GmbH",
    "aktuellerBestand": 150,
    "sicherheitsbestand": 50,
    "bestellpunkt": 80,
    "standardBestellmenge": 200,
    "einkaufspreis": 0.12,
    "status": "AKTIV"
  }
]
```

---

## GET /api/v1/artikel/{id}

**Beschreibung**: Gibt einen einzelnen Artikel anhand der internen Datenbank-ID zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Artikel-ID |

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/artikel/1' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Wie einzelnes Objekt aus der Liste-Response (siehe oben).

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Artikel mit dieser ID nicht gefunden |

---

## POST /api/v1/artikel

**Beschreibung**: Legt einen neuen Artikel an.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Body

```json
{
  "artikelnummer": "ART-002",
  "bezeichnung": "Mutter M6",
  "mengeneinheit": "Stk",
  "warengruppe": "Befestigungsmaterial",
  "lieferantId": 2,
  "sicherheitsbestand": 30,
  "bestellpunkt": 50,
  "standardBestellmenge": 100,
  "einkaufspreis": 0.08
}
```

### Validierungsregeln

| Feld | Pflicht | Regel |
|---|---|---|
| `artikelnummer` | Ja | Nicht leer, max. 50 Zeichen, systemweit eindeutig |
| `bezeichnung` | Ja | Nicht leer, max. 200 Zeichen |
| `mengeneinheit` | Ja | Nicht leer, max. 20 Zeichen |
| `warengruppe` | Ja | Nicht leer, max. 100 Zeichen |
| `lieferantId` | Nein | Muss existieren, falls angegeben |
| `sicherheitsbestand` | Ja | ≥ 0 |
| `bestellpunkt` | Ja | ≥ 0 |
| `standardBestellmenge` | Ja | ≥ 1 |
| `einkaufspreis` | Ja | > 0 |

### Response (201 Created)

Ersteller Artikel als vollständiges `ArtikelDto` (gleiche Struktur wie GET-Response).

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler (fehlende/ungültige Felder) |
| `409` | `artikelnummer` bereits vorhanden |

---

## PUT /api/v1/artikel/{id}

**Beschreibung**: Aktualisiert die Stammdaten eines vorhandenen Artikels (außer Bestand und Artikelnummer).  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Artikel-ID |

### Request-Body

Gleiche Felder wie `ArtikelCreateRequest`, jedoch ohne `artikelnummer` (unveränderlich).

### Response (200 OK)

Aktualisierter Artikel als `ArtikelDto`.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler |
| `404` | Artikel nicht gefunden |

---

## PUT /api/v1/artikel/{id}/deaktivieren

**Beschreibung**: Setzt den Artikel-Status auf `INAKTIV`. Deaktivierte Artikel nehmen nicht mehr an der Reorder-Analyse teil.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Artikel-ID |

### Request-Beispiel

```bash
curl -X PUT 'http://localhost/api/v1/artikel/1/deaktivieren' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Aktualisierter Artikel mit `"status": "INAKTIV"`.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Artikel nicht gefunden |

---

## GET /api/v1/artikel/{id}/bestand

**Beschreibung**: Gibt den aktuellen Lagerbestand eines Artikels zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/artikel/1/bestand' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
{ "aktuellerBestand": 150 }
```

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Artikel nicht gefunden |
