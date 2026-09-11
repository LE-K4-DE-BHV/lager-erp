---
Titel: files/-Verzeichnisstruktur mit .gitkeep anlegen
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Lege die gesamte `files/`-Verzeichnisstruktur im Repository an. Diese Ordner werden als Docker-Volume (`./files:/app/files`) in den Backend-Container gemountet und sind zwingend für den Batch-Import-Service erforderlich.

Referenz: Masterplan Unit 1, Abschnitt "Dateistruktur für files/-Volume"

**Zu erstellende Verzeichnisse (mit `.gitkeep`):**
```
files/
├── Input/
│   ├── Stammdaten/
│   └── Transaktionen/
├── Processed/
│   ├── Success/
│   ├── Warning/
│   └── Error/
└── Output/
    ├── Reports/
    └── Orders/
```

Jeder Unterordner erhält eine leere `.gitkeep`-Datei, damit die Verzeichnisstruktur in Git versioniert wird.

## Akzeptanzkriterien

- [x] Alle 7 Unterordner unter `files/` sind angelegt
- [x] Jeder Unterordner enthält eine `.gitkeep`-Datei
- [x] `.gitignore` schließt `files/**` aus, aber erlaubt `!files/**/.gitkeep`
- [x] Volume-Mount `./files:/app/files` in `docker-compose.yml` ist kompatibel

Implementierung abgeschlossen.
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-projektstruktur.md
