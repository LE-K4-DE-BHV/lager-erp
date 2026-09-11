---
Titel: Vue 3 Projektstruktur – Vite, package.json, main.js, App.vue
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: High
---

## Beschreibung

Initialisiere das Vue 3 Frontend-Projekt mit Vite als Build-Tool. Konfiguriere alle notwendigen Dependencies und richte die Grundstruktur (Verzeichnisse, `main.js`, `App.vue`) ein. PrimeVue 4.x wird als UI-Komponentenbibliothek verwendet.

Referenz: Masterplan Unit 9 — R-A3

**Zu erstellende Dateien:**
- `frontend/package.json`
- `frontend/vite.config.js`
- `frontend/index.html`
- `frontend/src/main.js`
- `frontend/src/App.vue`

**Dependencies (package.json):**
- `vue@3.x`
- `vue-router@4.x`
- `pinia@2.x`
- `primevue@4.x`
- `primeicons`
- `@primevue/themes` (Aura/Lara Theme)
- `axios@1.x`

**main.js — Initialisierung:**
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import router from './router'
import App from './App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(PrimeVue, { theme: { preset: Aura } })
app.mount('#app')
```

**App.vue:** Enthält `<AppHeader>` + `<RouterView>` + globalen `<Toast>`-Container (PrimeVue).

**Verzeichnisstruktur:**
```
frontend/src/
├── main.js
├── App.vue
├── router/
├── api/
│   ├── axios.js
│   └── services/
├── stores/
├── views/
└── components/
```

## Akzeptanzkriterien

- [x] `npm install` im `frontend/`-Verzeichnis laeuft ohne Fehler (196 packages, 2026-04-20)
- [x] `npm run build` kompiliert erfolgreich (293 Module, 8s, 2026-04-20)
- [x] Alle 7 Dependencies in `package.json` eingetragen
- [x] `main.js` initialisiert PrimeVue, Pinia, ToastService, ConfirmationService, Router
- [x] `App.vue` enthaelt `<RouterView>` und globalen `<Toast>`-Container
- [x] Verzeichnisstruktur entspricht dem Masterplan

Implementierung abgeschlossen. `package-lock.json` generiert (Dockerfile-npm-ci kompatibel).
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-projektstruktur.md
