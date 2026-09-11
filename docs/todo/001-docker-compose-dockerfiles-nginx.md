---
Titel: Docker Compose, Dockerfiles & Nginx-Konfiguration
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die vollständige Container-Infrastruktur für das Lager-Management-System. Das umfasst die `docker-compose.yml` mit drei Services (`db`, `backend`, `frontend`), Multi-Stage-Dockerfiles für Backend und Frontend sowie die Nginx-Konfiguration als Reverse-Proxy.

Referenz: Masterplan Unit 1 — R_3.1.2, R_3.1.3, R-A4

**Zu erstellende Dateien:**
- `docker-compose.yml` (drei Services: `db`, `backend`, `frontend`)
- `backend/Dockerfile` (Multi-Stage: `maven:3.9-eclipse-temurin-25` ? `eclipse-temurin:25-jre-alpine`)
- `frontend/Dockerfile` (Multi-Stage: `node:20-alpine` ? `nginx:alpine`)
- `frontend/nginx.conf` (Reverse-Proxy `/api/` ? `http://backend:8080`, SPA-Fallback)
- `.env.example` (Platzhalter für alle Secrets, kein `.env` selbst)

**Kerndetails docker-compose.yml:**
- `db`: Image `postgres:16-alpine`, Named Volume `db_data`, alle DB-Vars aus `.env`
- `backend`: Depends on `db`, Mount `./files:/app/files`, nur intern Port 8080 (kein `ports`-Mapping nach außen), Secrets aus `.env`
- `frontend`: Port `80:80` nach außen, enthält Nginx-Konfiguration

**nginx.conf Kernlogik:**
```nginx
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
location / {
    try_files $uri $uri/ /index.html;
}
```

**Wichtig:** Backend-Port (8080) und DB-Port (5432) dürfen NICHT via `ports` nach außen gemappt werden — nur internes Docker-Netzwerk.

## Akzeptanzkriterien

- [x] `docker-compose.yml` definiert drei Services (`db`, `backend`, `frontend`) mit korrekten Abhängigkeiten
- [x] `backend/Dockerfile` nutzt Multi-Stage-Build (Maven ? JRE-Alpine), Java 25, Non-Root-User `lager`
- [x] `frontend/Dockerfile` nutzt Multi-Stage-Build (Node ? Nginx-Alpine)
- [x] `frontend/nginx.conf` leitet `/api/**` an `backend:8080` weiter; SPA-Fallback auf `index.html` gesetzt
- [x] Nur der Frontend-Container (Port 80) ist nach außen erreichbar; Backend (8080) und DB (5432) laufen nur intern
- [x] `.env.example` mit allen benötigten Platzhaltern vorhanden (inkl. MAIL-Variablen)
- [x] MAIL-Variablen werden vollständig an den Backend-Container übergeben (ergänzt)
- [x] `.env` ist in `.gitignore` eingetragen
- [x] BCrypt-Hash `$$`-Escaping in `.env` und `.env.example` korrekt gesetzt (FEHLER-001 behoben, 2026-04-20)
- [x] `docker-compose up --build` startet alle Container ohne Fehler (Integrationstest bestätigt 2026-04-21)

## Notizen

? **FEHLER-001 (BEHOBEN 2026-04-20)**: BCrypt-Hash `$$`-Escaping in `.env` korrigiert.
`docker compose config` bestätigt vollen Hash. Details: `docs/test/2026-04-20-infrastruktur-konfiguration.md`
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-docker.md
