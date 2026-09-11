# Setup: Docker & Infrastruktur

**Ziel**: Du startest das vollständige Lager-Management-System (Backend, Frontend, Datenbank) mit einem einzigen Befehl lokal.

## Voraussetzungen

- Docker Desktop (Version 25+)
- Git
- Eine `.env`-Datei im Projekt-Root (siehe Abschnitt _Konfiguration_)

## Architektur

Das System besteht aus drei Docker-Services, die über ein internes Docker-Netzwerk kommunizieren:

| Service    | Image                        | Erreichbarkeit      |
|------------|------------------------------|---------------------|
| `db`       | `postgres:16-alpine`         | Nur intern (5432)   |
| `backend`  | Multi-Stage (Maven → JRE)    | Nur intern (8080)   |
| `frontend` | Multi-Stage (Node → Nginx)   | **Extern (Port 80)**|

Nach außen ist **ausschließlich Port 80** freigegeben. Nginx leitet `/api/`-Anfragen intern an `backend:8080` weiter.

## Setup

```bash
# 1. Repository klonen
git clone <repo-url>
cd lager-management

# 2. .env aus Vorlage erstellen
cp .env.example .env
# .env mit echten Werten befüllen (DB-Passwort, Operator-Passwort-Hash)

# 3. Alle Services bauen und starten
docker compose up --build

# 4. Im Hintergrund starten
docker compose up --build -d
```

Nach erfolgreichem Start ist die Anwendung unter [http://localhost](http://localhost) erreichbar.

## Konfiguration (.env)

Kopiere `.env.example` zu `.env` und befülle alle Pflichtfelder:

```env
# Datenbank
DB_NAME=lagerdb
DB_USER=lager
DB_PASSWORD=sicheres_passwort_hier

# Operator-Login (BCrypt-Hash, $$ für Docker-Escaping)
APP_OPERATOR_PASSWORD_HASH=$$2a$$12$$...dein_bcrypt_hash_hier...

# Dateipfad (Standard: /app/files, im Container gemountet)
FILES_BASE_PATH=/app/files

# E-Mail (optional)
MAIL_ENABLED=false
MAIL_HOST=localhost
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=lager@localhost
```

> **BCrypt-Escaping**: In der `.env`-Datei muss jedes `$` im Hash als `$$` geschrieben werden, damit Docker Compose den Wert korrekt interpoliert.

BCrypt-Hash für ein Passwort generieren (einmalig, lokal):

```bash
# Mit htpasswd (Apache-Tools)
htpasswd -bnBC 12 "" mein_passwort | tr -d ':\n' | sed 's/$$/$$/'

# Oder mit Spring Security CLI (falls verfügbar)
# Das Ergebnis beginnt mit: $$2a$$12$$...
```

## Volumes & Datenpersistenz

| Volume / Mount       | Inhalt                                      |
|----------------------|---------------------------------------------|
| `db_data`            | PostgreSQL-Datendateien (benanntes Volume)  |
| `./files:/app/files` | CSV/Excel-Importdateien & generierte PDFs   |

Das `files`-Verzeichnis wird vom Host-System in den Backend-Container gemountet. Es enthält folgende Unterordner:

```
files/
├── Input/
│   ├── Stammdaten/     # CSV/Excel für Lieferanten & Artikel
│   └── Transaktionen/  # CSV für Transaktionen
├── Output/
│   └── Orders/         # Generierte PDF-Bestellungen
└── Processed/          # Verarbeitete Dateien (Success/Warning/Error)
```

## Nginx-Konfiguration

Nginx (`frontend/nginx.conf`) übernimmt zwei Aufgaben:

1. **Reverse Proxy**: `/api/**` wird an `http://backend:8080` weitergeleitet.
2. **SPA-Fallback**: Alle anderen Routen liefern `index.html` aus (Vue Router History Mode).

```nginx
location /api/ {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_read_timeout 60s;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

## Nützliche Befehle

```bash
# Logs aller Services ansehen
docker compose logs -f

# Nur Backend-Logs
docker compose logs -f backend

# Container stoppen
docker compose down

# Container stoppen und Volumes löschen (ACHTUNG: löscht alle DB-Daten)
docker compose down -v

# Einzelnen Service neu bauen
docker compose up --build backend
```

## Fehlerbehebung

### Problem: Backend startet nicht – `APPLICATION FAILED TO START`
- **Ursache**: `APP_OPERATOR_PASSWORD_HASH` ist leer oder fehlt in der `.env`.
- **Lösung**: Trage einen gültigen BCrypt-Hash in der `.env` ein. Das System startet bewusst nicht ohne diesen Wert (Fail-Fast-Prinzip).

### Problem: `db` ist nicht gesund – Backend wartet
- **Ursache**: PostgreSQL braucht einen Moment beim ersten Start.
- **Lösung**: Warte 30 Sekunden. Der `backend`-Service startet erst, wenn `db` den Healthcheck besteht.

### Problem: Port 80 ist bereits belegt
- **Ursache**: Ein anderer lokaler Webserver läuft auf Port 80.
- **Lösung**: Stoppe den anderen Service oder ändere das Port-Mapping in `docker-compose.yml` temporär auf z.B. `"8090:80"`.
