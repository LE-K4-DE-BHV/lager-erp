---
Titel: Entity ImportLog + ImportLogRepository
Status: [DONE]

## Implementierungsnotiz
Implementierung abgeschlossen. `ImportLog.java` mit Lombok-Annotationen und `@CreatedDate` erstellt.
`dateiHash` mapped auf Spalte `datei_hash` (schema.sql). `ImportLogRepository` mit `existsByDateiHash()` für Idempotenz-Check.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `ImportLog` und das zugehörige Repository. `ImportLog` speichert den SHA-256-Hash jeder verarbeiteten Import-Datei und verhindert dadurch Doppelverarbeitungen. Diese Entity ist die Grundlage für die Idempotenz des Batch-Import-Services.

Referenz: Masterplan Unit 3 — R-B1, Abschnitt "Doppelverarbeitung verhindern", Tabelle `import_log` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/ImportLog.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/ImportLogRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK)
- `dateiname` (VARCHAR 500, NOT NULL)
- `dateiHash` (VARCHAR 64, UNIQUE, NOT NULL) — SHA-256 Hex-String
- `typ` (VARCHAR 50, NOT NULL) — `STAMMDATEN_ARTIKEL` | `STAMMDATEN_LIEFERANTEN` | `TRANSAKTIONEN_EINGANG` | `TRANSAKTIONEN_AUSGANG`
- `status` (VARCHAR 20, NOT NULL) — `SUCCESS` | `WARNING` | `ERROR`
- `zeilenGesamt` (INTEGER, optional)
- `zeilenErfolgreich` (INTEGER, optional)
- `zeilenFehlerhaft` (INTEGER, optional)
- `fehlerDetails` (TEXT, optional) — Freitext mit Fehler-Details
- `verarbeitetAm` (TIMESTAMP, `@CreatedDate`)

**Repository-Methoden:**
```
existsByDateiHash(String hash) → boolean  ← Kernmethode für Doppelverarbeitungs-Check
```

## Akzeptanzkriterien

- [ ] `ImportLog.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `dateiHash` hat `@Column(unique = true, nullable = false, length = 64)`
- [ ] `existsByDateiHash(String hash)` ist in `ImportLogRepository` deklariert
- [ ] Die Entity kann per `ImportLogRepository.save()` korrekt in die DB geschrieben werden
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-import.md
