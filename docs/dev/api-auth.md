# API: Authentifizierung

Die Auth-API stellt drei Endpunkte für Login, Logout und Session-Prüfung bereit. Alle anderen API-Endpunkte erfordern eine gültige Session.

---

## POST /api/v1/auth/login

**Beschreibung**: Authentifiziert den Benutzer und erstellt eine Session. Der Endpunkt wird vollständig vom Spring-Security-`formLogin`-Filter verarbeitet.  
**Authentifizierung**: Keiner (öffentlicher Endpunkt).

### Parameter (Body)

Content-Type: `application/x-www-form-urlencoded`

| Parameter | Typ | Erforderlich | Beschreibung |
|---|---|---|---|
| `username` | `String` | Ja | Benutzername (`operator`) |
| `password` | `String` | Ja | Klartextpasswort |

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/auth/login' \
  -c cookies.txt \
  -d 'username=operator&password=mein_passwort'
```

### Response (200 OK)

Das Cookie `JSESSIONID` wird automatisch im Response-Header gesetzt. Alle weiteren Requests senden dieses Cookie mit.

```json
{ "username": "operator" }
```

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `401` | Falsches Passwort oder unbekannter Benutzername |

```json
{ "status": 401, "message": "Ungültige Anmeldedaten" }
```

---

## GET /api/v1/auth/me

**Beschreibung**: Gibt den Benutzernamen der aktiven Session zurück. Wird vom Frontend für den Auth-Guard des Vue Routers genutzt.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X GET 'http://localhost/api/v1/auth/me' \
  -b cookies.txt
```

### Response (200 OK)

```json
{ "username": "operator" }
```

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `401` | Keine gültige Session vorhanden |

---

## POST /api/v1/auth/logout

**Beschreibung**: Invalidiert die aktuelle Session und löscht den Security-Kontext.  
**Authentifizierung**: Session-Cookie `JSESSIONID` (HttpOnly).

### Request-Beispiel

```bash
curl -X POST 'http://localhost/api/v1/auth/logout' \
  -b cookies.txt
```

### Response (204 No Content)

Kein Body. Nach dem Logout ist das Session-Cookie ungültig.

### Fehlerantworten

| Statuscode | Ursache |
|---|---|
| `401` | Keine gültige Session vorhanden |
