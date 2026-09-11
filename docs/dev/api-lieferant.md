# API: Lieferanten

Die Lieferanten-API gibt Lieferantenstammdaten aus und erlaubt deren Aktualisierung. Lieferanten werden primär per CSV/Excel-Import angelegt.

---

## GET /api/v1/lieferanten

**Beschreibung**: Gibt alle Lieferanten zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/lieferanten' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

```json
[
  {
    "id": 1,
    "lieferantId": "LIF-001",
    "name": "Metallbau GmbH",
    "kontaktEmail": "bestellung@metallbau.de",
    "kontaktTelefon": "+49 89 12345",
    "leadTimeTage": 3
  }
]
```

---

## GET /api/v1/lieferanten/{id}

**Beschreibung**: Gibt einen einzelnen Lieferanten anhand der internen Datenbank-ID zurück.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Lieferanten-ID |

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/lieferanten/1' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Einzelnes `LieferantDto`-Objekt (gleiche Struktur wie Liste-Response).

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `404` | Lieferant mit dieser ID nicht gefunden |

---

## PUT /api/v1/lieferanten/{id}

**Beschreibung**: Aktualisiert die Kontaktdaten und die Lieferzeit eines Lieferanten. Die `lieferant_id` (Geschäftsnummer) ist unveränderlich.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `id` | `Long` (Path) | Ja | Interne Lieferanten-ID |

### Request-Body

```json
{
  "name": "Metallbau GmbH & Co. KG",
  "kontaktEmail": "neu@metallbau.de",
  "kontaktTelefon": "+49 89 99999",
  "leadTimeTage": 5
}
```

### Validierungsregeln

| Feld | Pflicht | Regel |
|---|---|---|
| `name` | Ja | Nicht leer, max. 200 Zeichen |
| `kontaktEmail` | Nein | — |
| `kontaktTelefon` | Nein | — |
| `leadTimeTage` | Ja | ≥ 1 |

### Response (200 OK)

Aktualisierter Lieferant als `LieferantDto`.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Validierungsfehler |
| `404` | Lieferant nicht gefunden |
