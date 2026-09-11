---
Titel: Entity Transaktion + TransaktionRepository
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `Transaktion` und das zugehörige Repository. `Transaktion` erfasst alle Warenbewegungen — sowohl aus dem Batch-Import (`quelle=BATCH`) als auch manuelle Buchungen (`quelle=MANUELL`). Diese Tabelle ist die Grundlage für die Verbrauchsanalyse im Reorder-Job und die Transaktionshistorie.

Referenz: Masterplan Unit 3 — R-M4, R-T1, Tabelle `transaktionen` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/Transaktion.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/TransaktionRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK)
- `artikel` (`@ManyToOne`, FK zu `artikel.id`, NOT NULL)
- `typ` (`TransaktionTyp`, `@Enumerated(EnumType.STRING)`, NOT NULL) — `EINGANG` | `AUSGANG`
- `buchungstyp` (VARCHAR 50, optional) — `WARENEINGANG` | `Verbrauch intern` | `Verkauf` | `Verlust/Schwund` | `Retoure`
- `menge` (INTEGER, NOT NULL)
- `datum` (DATE, NOT NULL)
- `quelle` (`TransaktionQuelle`, `@Enumerated(EnumType.STRING)`, DEFAULT `BATCH`)
- `bestellung` (`@ManyToOne`, FK zu `bestellungen.id`, optional)
- `lieferant` (`@ManyToOne`, FK zu `lieferanten.id`, optional)
- `grund` (TEXT, optional) — Freitext
- `benutzerId` (VARCHAR 100, optional) — Audit Trail bei manuellen Buchungen
- `erstelltAm` (TIMESTAMP, `@CreatedDate`)

**Repository-Methoden (Custom Queries):**
```
findByArtikelIdAndTypAndDatumBetween(Long artikelId, TransaktionTyp typ, LocalDate von, LocalDate bis)
findAllByArtikelIdOrderByDatumDesc(Long artikelId)
findAllByDatumBetween(LocalDate von, LocalDate bis, Pageable pageable)
```

## Implementierungsnotiz

Implementierung abgeschlossen. `mvn compile` → BUILD SUCCESS.
Erstellte Dateien:
- `domain/entity/Transaktion.java`
- `domain/repository/TransaktionRepository.java`

## Akzeptanzkriterien

- [ ] `Transaktion.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `typ` und `quelle` nutzen `@Enumerated(EnumType.STRING)` mit korrekten Enum-Typen
- [ ] `findByArtikelIdAndTypAndDatumBetween` ermöglicht die Verbrauchsanalyse im Reorder-Job
- [ ] `findAllByDatumBetween` mit `Pageable` ermöglicht paginierte Transaktionshistorie
- [ ] Alle 3 Custom-Repository-Methoden sind deklariert
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-transaktion.md
