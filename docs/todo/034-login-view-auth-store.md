---
Titel: LoginView + Pinia AuthStore + AppHeader (Logout)
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: High
---

## Beschreibung

Implementiere die Login-Seite als PrimeVue-Formular, den Pinia-basierten Auth-Store für den globalen Session-Zustand und den AppHeader mit Logout-Funktionalität.

Referenz: Masterplan Unit 9 — Abschnitt "LoginView.vue" + "AppHeader.vue"

**Zu erstellende Dateien:**
- `frontend/src/views/LoginView.vue`
- `frontend/src/stores/authStore.js` (Pinia)
- `frontend/src/components/AppHeader.vue`

**LoginView.vue:**
- PrimeVue Komponenten: `InputText`, `Password`, `Button`, `Message`
- Ruft `authService.login(username, password)` auf
- Bei Erfolg: Weiterleitung zu `/dashboard`
- Bei Fehler: Inline-Fehlermeldung via PrimeVue `Message` (severity: `error`)
- Keine Speicherung von Credentials im `localStorage` oder `sessionStorage`

**Pinia AuthStore (`authStore.js`):**
```javascript
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = computed(() => user.value !== null)

  async function fetchUser() {
    const response = await authService.me()
    user.value = response.data
  }

  async function logout() {
    await authService.logout()
    user.value = null
    router.push('/login')
  }

  return { user, isAuthenticated, fetchUser, logout }
})
```

**AppHeader.vue:**
- Zeigt Anwendungstitel + Navigation-Links zu allen Hauptseiten
- Logout-Button ruft `authStore.logout()` auf
- PrimeVue `Menubar` oder `Toolbar`

## Akzeptanzkriterien

- [x] Login-Formular nutzt PrimeVue-Komponenten (`InputText`, `Password`, `Button`)
- [x] Bei erfolgreichem Login: Weiterleitung zu `/dashboard`
- [x] Bei fehlgeschlagenem Login: Inline-Fehlermeldung (kein Alert/Redirect)
- [x] Kein Passwort wird im Store oder LocalStorage gespeichert
- [x] `authStore` nutzt Pinia `defineStore` mit Composition API (`ref`, `computed`)
- [x] `AppHeader` zeigt Logout-Button, der Session beendet und zu `/login` umleitet
- [x] Nach Logout: kein weiterer API-Zugriff möglich ohne erneuten Login

Implementierung abgeschlossen. Vitest-Tests in `src/__tests__/loginView.test.js` — 4/4 grün (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/user/login.md
