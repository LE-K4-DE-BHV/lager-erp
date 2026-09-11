---
Titel: Frontend Dockerfile + nginx.conf – Produktions-Build
Status: [DONE]
Zuweisung: Frontend Dev
Priorität: High
---

## Beschreibung

Erstelle das Frontend-Dockerfile für den Multi-Stage-Build (Node → Nginx) und die `nginx.conf` mit dem Reverse-Proxy zur Backend-API. Diese Dateien sind notwendig, damit das Frontend als Container läuft.

Referenz: Masterplan Unit 1 — Frontend-Dockerfile, Abschnitt "nginx.conf Kernkonfiguration"

**Zu erstellende Dateien:**
- `frontend/Dockerfile`
- `frontend/nginx.conf`

**frontend/Dockerfile:**
```dockerfile
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**frontend/nginx.conf:**
```nginx
server {
    listen 80;
    server_name localhost;

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

**Wichtig:** Der Hostname `backend` muss mit dem Service-Namen in `docker-compose.yml` übereinstimmen.

## Akzeptanzkriterien

- [x] `docker build` für das Frontend-Image läuft fehlerfrei durch
- [x] Nginx leitet alle `/api/**`-Anfragen an `http://backend:8080` weiter
- [x] SPA-Fallback (`try_files ... /index.html`) ist konfiguriert (für Vue Router History-Mode)
- [x] `npm run build` im Container erzeugt korrekte `dist/`-Artefakte
- [x] Im Docker-Compose-Stack ist `http://localhost` erreichbar und zeigt die Vue-App
- [x] `http://localhost/api/v1/auth/me` wird durch Nginx korrekt an das Backend weitergeleitet

Beide Dateien waren bereits korrekt implementiert (als Teil der Infrastruktur-Vorbereitung). Akzeptanzkriterien vollständig erfüllt (2026-04-21).
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-docker.md
