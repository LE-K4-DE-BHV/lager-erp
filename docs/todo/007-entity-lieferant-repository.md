---
Titel: Entity Lieferant + LieferantRepository
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `Lieferant` und das zugehörige Spring Data Repository. Der `Lieferant` ist die Basis-Entity, da `Artikel` und `Bestellung` per FK darauf referenzieren.

Referenz: Masterplan Unit 3 — R_2.1.2, Tabelle `lieferanten` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/Lieferant.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/LieferantRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK, `@GeneratedValue(strategy = IDENTITY)`)
- `lieferantId` (VARCHAR 50, UNIQUE, NOT NULL) — die externe Geschäfts-ID (z.B. "LF-001")
- `name` (VARCHAR 200, NOT NULL)
- `kontaktEmail` (VARCHAR 200, optional)
- `kontaktTelefon` (VARCHAR 50, optional)
- `leadTimeTage` (INTEGER, NOT NULL, DEFAULT 1)
- `erstelltAm` (TIMESTAMP, NOT NULL, `@CreatedDate`)

**Lombok-Annotationen:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

**Repository-Methoden:**
- `findByLieferantId(String lieferantId)` → `Optional<Lieferant>`
- `existsByLieferantId(String lieferantId)`

## Implementierungsnotiz

Implementierung abgeschlossen. `JpaAuditingConfig.java` mit `@EnableJpaAuditing` in `config/` erstellt.
`Lieferant.java` und `LieferantRepository.java` in `domain/entity/` und `domain/repository/` angelegt.
Kompilierung: BUILD SUCCESS.

## Akzeptanzkriterien

- [ ] `Lieferant.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `@Entity`, `@Table(name = "lieferanten")` sind gesetzt
- [ ] `lieferantId` hat `@Column(unique = true, nullable = false)`
- [ ] `erstelltAm` nutzt `@CreatedDate` von Spring Data JPA Auditing
- [ ] `LieferantRepository` extends `JpaRepository<Lieferant, Long>`
- [ ] Custom-Methoden `findByLieferantId` und `existsByLieferantId` sind deklariert
- [ ] Keine Getter/Setter von Hand geschrieben (Lombok übernimmt das)
---




---
**Doku erstellt (2026-04-28)**: docs/dev/api-lieferant.md
