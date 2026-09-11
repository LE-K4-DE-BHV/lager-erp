---
Titel: Axios-Konfiguration + Auth-API-Service
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: High
---

## Beschreibung

Konfiguriere Axios als zentralen HTTP-Client für alle API-Aufrufe. Der `authService` kapselt alle Auth-bezogenen API-Calls. Der 401-Interceptor leitet den Nutzer automatisch zur Login-Seite weiter, wenn die Session abgelaufen ist.

Referenz: Masterplan Unit 9 — R-A3, Abschnitt "axios.js – Zentralkonfiguration"

**Zu erstellende Dateien:**
- `frontend/src/api/axios.js`
- `frontend/src/api/services/authService.js`

**axios.js — Konfiguration:**
```javascript
import axios from 'axios'
import router from '../router'

const apiClient = axios.create({
  baseURL: '/api/v1',        // relativ → Nginx-Proxy leitet weiter
  withCredentials: true,     // JSESSIONID-Cookie automatisch mitsenden
})

// Response-Interceptor: bei 401 → Login-Seite
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default apiClient
```

**authService.js:**
```javascript
import api from '../axios'

export const authService = {
  login: (username, password) => api.post('/auth/login', {}, {
    auth: { username, password }
  }),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
}
```

**Wichtig:** Kein manuelles Token-Handling im Frontend — das `JSESSIONID`-Cookie übernimmt die gesamte Session-Verwaltung. `withCredentials: true` ist zwingend.

## Akzeptanzkriterien

- [x] `axios.js` nutzt `baseURL: '/api/v1'` (relativ, kein absoluter Host)
- [x] `withCredentials: true` ist gesetzt
- [x] Response-Interceptor leitet bei `401` auf `/login` um (9/9 Tests gruen, 2026-04-20)
- [x] Kein Bearer-Token, kein SessionStorage — ausschliesslich Cookie-basierte Auth
- [x] `authService.login()`, `authService.logout()`, `authService.me()` implementiert
- [x] `authService.js` importiert den konfigurierten `apiClient` aus `axios.js`

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/` — 9/9 gruen.
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-uebersicht.md
