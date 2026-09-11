---
Titel: FileService – SHA-256-Hashing & Dateiverschiebung
Status: [DONE]

### Implementierungsnotiz
Implementierung abgeschlossen. `AppFilesProperties.java` in `config/` erstellt. `FileService.java` in `service/` erstellt.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere den `FileService` als zentrale Utility-Klasse für alle Dateioperationen des Batch-Import-Systems. Dieser Service kapselt das Scannen von Verzeichnissen, die SHA-256-Berechnung und das Verschieben verarbeiteter Dateien in die Processed-Ordner.

Referenz: Masterplan Unit 5 — R-B1, Abschnitt "FileService (Kernoperationen)"

**Zu erstellende Datei:**
- `backend/src/main/java/com/lagermanagement/space/service/FileService.java`

**Methoden:**

```java
// Listet alle *.csv und *.xlsx Dateien in einem Verzeichnis
List<Path> scanDirectory(Path dir, String pattern)

// Berechnet SHA-256-Hash als Hex-String
String computeHash(Path file)

// Verschiebt Datei in Zielordner (erstellt YYYY-MM-DD/-Unterordner)
void moveFile(Path source, Path targetDir)

// Gibt absoluten Pfad relativ zu app.files.base-path zurück
Path resolveOutputPath(String subPath)
```

**Verzeichnis-Struktur bei moveFile:**
- Ziel: `Processed/{Success|Warning|Error}/YYYY-MM-DD/dateiname.csv`
- Der `YYYY-MM-DD`-Unterordner wird automatisch erstellt, falls nicht vorhanden.

**Konfiguration:** `app.files.base-path` kommt aus `application.yml` via `@ConfigurationProperties`.

**Sicherheitshinweis:** Niemals Dateipfade in Log-Ausgaben schreiben, die absolute Systempfade enthalten.

## Akzeptanzkriterien

- [ ] `FileService` ist ein `@Service` mit `@RequiredArgsConstructor` (kein `@Autowired`)
- [ ] `computeHash()` berechnet den SHA-256-Hash als 64-stelligen Hex-String
- [ ] `moveFile()` erstellt den Zielordner (`YYYY-MM-DD/`) automatisch, falls nicht vorhanden
- [ ] `scanDirectory()` gibt nur Dateien zurück, die dem Pattern entsprechen (z.B. `*.csv`, `*.xlsx`)
- [ ] `resolveOutputPath()` kombiniert `app.files.base-path` mit dem übergebenen Unterpfad korrekt
- [ ] Keine absoluten Pfade in Log-Ausgaben
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
