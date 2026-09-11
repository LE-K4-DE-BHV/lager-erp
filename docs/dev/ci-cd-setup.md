# CI/CD-Setup – Lager-Management-System

**Stand:** September 2026
**Plattform:** GitHub Actions (YAML)
**Runner:** `ubuntu-latest` (GitHub-hosted, Docker ist vorinstalliert)
**Deployment-Ziel:** `docker-compose` auf einem lokalen/internen Server

---

## Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [Workflow-Jobs](#workflow-jobs)
3. [Benötigte GitHub Actions Secrets](#benötigte-github-actions-secrets)
4. [Workflow ist bereits registriert](#workflow-ist-bereits-registriert)
5. [CD aktivieren](#cd-aktivieren)
6. [Fehlerbehebung (Troubleshooting)](#fehlerbehebung-troubleshooting)

---

## Übersicht

Der Workflow (`.github/workflows/ci.yml`) läuft bei jedem Push auf `main` sowie bei Pull Requests gegen `main` — aber nur, wenn sich Dateien in `backend/**` oder `frontend/**` geändert haben.

```
Trigger: Push auf main / PR gegen main
         (nur bei Änderungen in backend/** oder frontend/**)
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
      backend                         frontend
   (Java 25, mvn verify)           (Node 20, Vitest)
   Docker Build (kein Push)         Docker Build (kein Push)
          │                               │
          └───────────────┬───────────────┘
                          ▼
                  [CD Deploy – TODO]
               docker compose pull && up -d
```

Die beiden Jobs laufen **parallel** auf GitHub-hosted `ubuntu-latest`-Runnern. Docker ist auf diesen Runnern bereits vorinstalliert — anders als früher bei Azure DevOps ist **kein Self-Hosted Agent nötig**, weder für Java 25 (via `actions/setup-java`) noch für Testcontainers (Docker-Daemon des Runners).

Ein CD-Job ist noch nicht enthalten und wird erst ergänzt, wenn die Infrastruktur (Container Registry + Zugang zum Zielserver) bereit ist.

---

## Workflow-Jobs

### Job: `backend`

| Schritt | Beschreibung |
|---------|--------------|
| `actions/setup-java@v4` | Installiert Java 25 (Temurin) und cacht `.m2`-Repository (Key basiert auf `backend/pom.xml`) |
| `mvn verify` | Kompiliert, führt Unit- und Integrationstests aus (inkl. Testcontainers/PostgreSQL) |
| Testergebnisse publizieren | JUnit-Reports aus `target/surefire-reports/` via `dorny/test-reporter`, auch bei Testfehlern (`if: always()`) |
| Docker Build | Baut `lager-backend:${{ github.sha }}` aus `backend/Dockerfile` (kein Push) |

**Hinweis zu Testcontainers:** Der Docker-Daemon des GitHub-hosted Runners wird automatisch genutzt — keine zusätzliche Konfiguration nötig.

### Job: `frontend`

| Schritt | Beschreibung |
|---------|--------------|
| `actions/setup-node@v4` | Installiert Node 20 und cacht `node_modules` (Key basiert auf `frontend/package-lock.json`) |
| `npm ci` | Installiert Abhängigkeiten reproduzierbar |
| Vitest Tests | `npm run test -- --run` (kein Watch-Modus, bricht bei Fehler ab) |
| Docker Build | Baut `lager-frontend:${{ github.sha }}` aus `frontend/Dockerfile` (kein Push) |

### CD (noch nicht enthalten)

Wird erst ergänzt, wenn folgendes bereit ist:
- Container-Registry (empfohlen: GitHub Container Registry `ghcr.io`, Auth über `secrets.GITHUB_TOKEN`)
- Zugang zum Zielserver (z. B. SSH-Key als GitHub Secret)
- `.env` auf dem Zielserver hinterlegt

Führt auf dem Zielserver aus: `docker compose pull && docker compose up -d --remove-orphans`

---

## Benötigte GitHub Actions Secrets

### Für CI (aktuell nicht nötig)

Der CI-Workflow benötigt aktuell **keine** Secrets — Caching und Docker-Build laufen ohne externe Zugangsdaten.

### Für CD (sobald aktiviert)

| Secret | Zweck |
|--------|-------|
| `DEPLOY_SSH_KEY` | Privater SSH-Key für den Zielserver |
| `DEPLOY_HOST` | Hostname/IP des Zielservers |
| `DEPLOY_USER` | SSH-Benutzer auf dem Zielserver |
| `DB_PASSWORD` | Datenbankpasswort (falls im Deploy-Schritt benötigt) |
| `APP_OPERATOR_PASSWORD_HASH` | BCrypt-Hash des Operator-Passworts |

Secrets anlegen: **Repository → Settings → Secrets and variables → Actions → New repository secret**

---

## Workflow ist bereits registriert

GitHub Actions erkennt jede YAML-Datei unter `.github/workflows/` automatisch — es ist **keine manuelle Registrierung** wie bei Azure DevOps nötig. Sobald `.github/workflows/ci.yml` auf `main` liegt, läuft der Workflow bei jedem passenden Push oder Pull Request automatisch. Den Status siehst du im Reiter **Actions** des Repositories.

---

## CD aktivieren

Sobald die Infrastruktur bereit ist, folgende Schritte ausführen:

### Voraussetzungen prüfen

- [ ] Container-Registry gewählt (empfohlen: `ghcr.io`, kein zusätzliches Secret nötig)
- [ ] SSH-Zugang zum Zielserver eingerichtet, `DEPLOY_SSH_KEY` / `DEPLOY_HOST` / `DEPLOY_USER` als Secrets hinterlegt
- [ ] `.env`-Datei auf dem Zielserver unter `/opt/lager_management/.env` hinterlegt
- [ ] `docker-compose.yml` auf dem Zielserver unter `/opt/lager_management/docker-compose.yml` vorhanden

### Aktivierungsschritte in `.github/workflows/ci.yml`

1. **Docker Push für Backend aktivieren** (im Job `backend`):
   Die auskommentierten `docker/login-action` + Push-Schritte einkommentieren.

2. **Docker Push für Frontend aktivieren** (im Job `frontend`):
   Analog zum Backend.

3. **Deploy-Job ergänzen** (am Ende der Datei), z. B. mit `appleboy/ssh-action`:
   ```yaml
   deploy:
     needs: [backend, frontend]
     if: github.ref == 'refs/heads/main' && github.event_name == 'push'
     runs-on: ubuntu-latest
     steps:
       - uses: appleboy/ssh-action@v1
         with:
           host: ${{ secrets.DEPLOY_HOST }}
           username: ${{ secrets.DEPLOY_USER }}
           key: ${{ secrets.DEPLOY_SSH_KEY }}
           script: |
             cd /opt/lager_management
             docker compose pull
             docker compose up -d --remove-orphans
             docker image prune -f
   ```

4. Workflow-Datei committen und auf `main` pushen — der Deploy-Job läuft beim nächsten Trigger.

### Deployment-Ablauf nach Aktivierung

```
Push auf main
    → backend  (Tests + Push → ghcr.io/.../lager-backend:sha)
    → frontend (Tests + Push → ghcr.io/.../lager-frontend:sha)
    → deploy   (SSH: docker compose pull && up -d)
```

---

## Fehlerbehebung (Troubleshooting)

### Problem: Java 25 nicht gefunden

**Ursache:** `actions/setup-java` wurde mit falscher `java-version` oder `distribution` konfiguriert.

**Lösung:** Sicherstellen, dass der Schritt exakt so aussieht:
```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "25"
```

**Prävention:** Bei Java-Updates im Projekt immer `pom.xml` (`<java.version>`), beide Dockerfiles und `.github/workflows/ci.yml` gemeinsam anpassen.

### Problem: Testcontainers schlägt fehl (`Cannot connect to Docker`)

**Ursache:** Sehr selten auf GitHub-hosted Runnern, da Docker vorinstalliert ist. Tritt eher bei einem eigenen Self-Hosted Runner ohne laufenden Docker-Daemon auf.

**Lösung:**
1. Bei GitHub-hosted Runnern: Job-Log auf abweichende Fehlermeldung prüfen (meist ein anderes Problem, z. B. Ressourcen-Timeout).
2. Bei einem eigenen Self-Hosted Runner: `docker info` auf dem Runner-Host ausführen, Docker-Dienst starten.

### Problem: `npm run test` hängt (kein Output, kein Abbruch)

**Ursache:** Vitest startet im Watch-Modus, wenn kein `--run` übergeben wird.

**Lösung:** Sicherstellen, dass der Test-Schritt exakt so aufgerufen wird:
```bash
npm run test -- --run
```
Das `--` ist wichtig: Es trennt npm-Argumente von den Vitest-Argumenten.

### Problem: Maven- oder npm-Cache wird nicht wiederverwendet

**Ursache:** Der Cache-Key von `actions/setup-java` (`cache: maven`) bzw. `actions/setup-node` (`cache: npm`) basiert auf dem jeweiligen Lockfile (`pom.xml` bzw. `package-lock.json`). Bei jeder Änderung wird ein neuer Cache angelegt.

**Lösung:** Das ist das erwartete Verhalten — GitHub Actions cached automatisch mit Fallback auf den letzten passenden Cache-Präfix.

### Problem: Docker Build schlägt fehl (`context not found` / `COPY failed`)

**Ursache:** Der Build-Context muss auf das Verzeichnis zeigen, das das `Dockerfile` enthält (nicht den Repository-Root, da relative `COPY`-Pfade im Dockerfile verwendet werden).

**Lösung:** Im Workflow ist `working-directory: backend` bzw. `working-directory: frontend` gesetzt und der Docker-Build läuft mit Context `.` (relativ zum Arbeitsverzeichnis). Das Dockerfile liegt in `backend/Dockerfile` bzw. `frontend/Dockerfile` — damit wird `COPY pom.xml .` korrekt aufgelöst.
