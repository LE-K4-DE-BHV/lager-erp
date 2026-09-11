---
Titel: Entity Bestellung + BestellungRepository
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `Bestellung` und das zugehörige Repository. `Bestellung` repräsentiert eine freigegebene Bestellung (Status `OFFEN` → `GELIEFERT`). Bestellungen sind nach Erstellung immutable — Status-Übergänge sind nur vorwärts erlaubt.

Referenz: Masterplan Unit 3 — R-L1, Tabelle `bestellungen` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/Bestellung.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/BestellungRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK)
- `bestellnummer` (VARCHAR 50, UNIQUE, NOT NULL) — Format: `BEST-{YYYY}-{NNN}`
- `artikel` (`@ManyToOne`, FK zu `artikel.id`, NOT NULL)
- `lieferant` (`@ManyToOne`, FK zu `lieferanten.id`, NOT NULL)
- `bestellmenge` (INTEGER, NOT NULL)
- `einkaufspreis` (NUMERIC(10,2), optional)
- `gewuensteresLieferdatum` (DATE, optional)
- `notiz` (TEXT, optional)
- `status` (VARCHAR 20, DEFAULT `'OFFEN'`) — `OFFEN` | `GELIEFERT`
- `erstelltVon` (VARCHAR 100, NOT NULL) — Audit Trail via `SecurityContextHolder`
- `erstelltAm` (TIMESTAMP, `@CreatedDate`)
- `aktualisiertAm` (TIMESTAMP, `@LastModifiedDate`)

**Repository-Methoden (Custom Queries):**
```
findByBestellnummer(String bestellnummer) → Optional<Bestellung>
findAllByArtikelIdAndStatus(Long artikelId, String status) → für Wareneingang-Prüfung
countByErstelltAmBetween(LocalDateTime von, LocalDateTime bis) → für Bestellnummer-Generierung
```

## Implementierungsnotiz

Implementierung abgeschlossen. `BestellungStatus.java` Enum (OFFEN, GELIEFERT) in `domain/enums/` angelegt.
`Bestellung.java` und `BestellungRepository.java` erstellt. Kompilierung: BUILD SUCCESS.

## Akzeptanzkriterien

- [ ] `Bestellung.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `bestellnummer` hat `@Column(unique = true, nullable = false)`
- [ ] `erstelltVon` ist NOT NULL — wird vom `OrderService` via `SecurityUtils.getCurrentUsername()` befüllt
- [ ] `@ManyToOne`-Beziehungen zu `Artikel` und `Lieferant` sind korrekt definiert
- [ ] Alle 3 Custom-Repository-Methoden sind deklariert
- [ ] Kein direkte Status-Änderung außerhalb des `OrderService` / `InventoryService`
---




---
**Doku erstellt (2026-04-28)**: docs/dev/api-bestellung.md
