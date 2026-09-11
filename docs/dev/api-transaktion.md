# API: Transaktionshistorie

Die Transaktions-API gibt alle gebuchten Warenbewegungen zurück – paginiert und filterbar.

---

## GET /api/v1/transaktionen

**Beschreibung**: Gibt eine paginierte Liste aller Transaktionen zurück. Filterbar nach Artikel und Zeitraum.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Parameter (Query)

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `artikelId` | `Long` | Nein | Filtert auf Transaktionen eines bestimmten Artikels |
| `von` | `LocalDate` (ISO) | Nein | Startdatum des Zeitraums (`YYYY-MM-DD`) |
| `bis` | `LocalDate` (ISO) | Nein | Enddatum des Zeitraums (`YYYY-MM-DD`) |
| `seite` | `int` | Nein | Seitennummer (0-basiert, Standard: `0`) |
| `groesse` | `int` | Nein | Einträge pro Seite (Standard: `50`) |

Sortierung: `datum` absteigend, dann `erstellt_am` absteigend.

### Request-Beispiele

```bash
# Alle Transaktionen (erste Seite)
curl -X GET 'http://localhost/api/v1/transaktionen' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'

# Gefiltert nach Artikel und Zeitraum
curl -X GET 'http://localhost/api/v1/transaktionen?artikelId=1&von=2026-04-01&bis=2026-04-30' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'

# Mit Paginierung
curl -X GET 'http://localhost/api/v1/transaktionen?seite=1&groesse=20' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

### Response (200 OK)

Spring-`Page`-Objekt mit eingebetteten Transaktionen:

```json
{
  "content": [
    {
      "id": 42,
      "artikelnummer": "ART-001",
      "artikelBezeichnung": "Schraube M6",
      "typ": "EINGANG",
      "buchungstyp": null,
      "menge": 200,
      "datum": "2026-04-28",
      "quelle": "MANUELL",
      "benutzerId": "operator",
      "bestellnummer": "BEST-2026-001"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "size": 50,
  "number": 0
}
```

| Feld | Werte | Beschreibung |
|---|---|---|
| `typ` | `EINGANG`, `AUSGANG` | Bewegungsrichtung |
| `quelle` | `MANUELL`, `BATCH` | Herkunft der Buchung |
| `buchungstyp` | z.B. `VERBRAUCH` | Nur bei Ausgängen, frei definierbar |
| `benutzerId` | `operator` | Nur bei manuellen Buchungen gesetzt |
| `bestellnummer` | z.B. `BEST-2026-001` | Nur bei bestellungsbezogenen Eingängen gesetzt |

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `400` | Ungültiges Datumsformat |
| `401` | Keine gültige Session |
