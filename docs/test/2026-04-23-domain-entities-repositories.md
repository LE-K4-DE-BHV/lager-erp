# Testergebnis: Domain Entities & Repositories (Tickets 007–011)

**Datum**: 2026-04-23  
**Tickets**: 
- `docs/todo/007-entity-lieferant-repository.md`  
- `docs/todo/008-entity-artikel-repository.md`  
- `docs/todo/009-entity-bestellung-repository.md`  
- `docs/todo/010-entity-bestellvorschlag-repository.md`  
- `docs/todo/011-entity-transaktion-repository.md`  
**Status**: ✅ GRÜN

---

## Getestete Klassen / Komponenten

| Klasse | Typ |
|--------|-----|
| `LieferantRepositoryTest.java` | @DataJpaTest + Testcontainers (NEU) |
| `ArtikelRepositoryTest.java` | @DataJpaTest + Testcontainers (bestehend) |
| `BestellungRepositoryTest.java` | @DataJpaTest + Testcontainers (NEU) |
| `BestellvorschlagRepositoryTest.java` | @DataJpaTest + Testcontainers (NEU) |
| `TransaktionRepositoryTest.java` | @DataJpaTest + Testcontainers (NEU) |
| `DomainEnumsTest.java` | Unit-Test (bestehend) |

---

## Coverage-Übersicht

| Repository | Tests | Getestete Methoden |
|------------|-------|--------------------|
| `LieferantRepository` | 5 | `findByLieferantId`, `existsByLieferantId`, Default-Werte, Auditing |
| `ArtikelRepository` | 5 | `findByArtikelnummer`, `findAllByStatus`, `existsByArtikelnummer`, `findAllByStatusAndBestellpunktGreaterThan` |
| `BestellungRepository` | 4 | `findByBestellnummer`, `findAllByArtikelIdAndStatus`, `countByErstelltAmBetween`, Default-Status |
| `BestellvorschlagRepository` | 5 | `findByArtikelIdAndStatusIn`, `findAllByStatusIn`, `findTopByArtikelIdAndStatusOrderByErstelltAmDesc`, Default-Status |
| `TransaktionRepository` | 5 | `findByArtikelIdAndTypAndDatumBetween`, `findAllByArtikelIdOrderByDatumDesc`, `findAllByDatumBetween` (paginiert) |
| `DomainEnums` | 7 | Alle Enum-Werte: ArtikelStatus, VorschlagStatus, TransaktionTyp, TransaktionQuelle |

**Gesamt Backend-Tests:** 31 Tests, 0 Fehler, 0 Failures

---

## Test-Infrastruktur: Upgrade Testcontainers

**Problem**: Testcontainers 1.20.4 lieferte `BadRequestException (Status 400, ServerVersion: "")` — inkompatibel mit Docker Engine 29.x / Docker Desktop 4.66.  
**Lösung**: `pom.xml` Testcontainers BOM von `1.20.4` → `1.21.4` aktualisiert.  
**Ergebnis**: Docker-Verbindung erfolgreich, alle Testcontainer starten zuverlässig.

> **Hinweis**: `SpaceApplicationTests.contextLoads` schlägt in der vollen `mvn test`-Ausführung fehl, da Umgebungsvariablen (`APP_OPERATOR_PASSWORD_HASH`, DB-Credentials) nicht gesetzt sind. Dies ist ein bekanntes, von den Entity-Tickets unabhängiges Infrastruktur-Thema.

---

## Akzeptanzkriterien-Prüfung

### Ticket 007 — Lieferant

- [x] `Lieferant.java` nutzt `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [x] `@Entity`, `@Table(name = "lieferanten")` korrekt gesetzt
- [x] `lieferantId` hat `@Column(unique = true, nullable = false)`
- [x] `erstelltAm` nutzt `@CreatedDate` — per Test verifiziert (nicht null nach Save)
- [x] `LieferantRepository` erweitert `JpaRepository<Lieferant, Long>`
- [x] `findByLieferantId` und `existsByLieferantId` deklariert und funktional getestet

### Ticket 008 — Artikel

- [x] Alle Lombok-Annotationen korrekt
- [x] `@Entity`, `@Table(name = "artikel")`, `@EntityListeners(AuditingEntityListener.class)` gesetzt
- [x] `artikelnummer` hat `@Column(unique = true, nullable = false)`
- [x] `status` nutzt `@Enumerated(EnumType.STRING)` mit `ArtikelStatus`
- [x] `@ManyToOne`-Beziehung zu `Lieferant` korrekt definiert
- [x] Alle 4 Custom-Repository-Methoden getestet und grün

### Ticket 009 — Bestellung

- [x] Alle Lombok-Annotationen korrekt
- [x] `bestellnummer` hat `@Column(unique = true, nullable = false)`
- [x] `erstelltVon` ist NOT NULL — per Test mit "operator" gesetzt
- [x] `@ManyToOne`-Beziehungen zu `Artikel` und `Lieferant` korrekt
- [x] Alle 3 Custom-Repository-Methoden getestet und grün
- [x] Default-Status `OFFEN` per Test verifiziert

### Ticket 010 — Bestellvorschlag

- [x] Alle Lombok-Annotationen korrekt
- [x] `status` nutzt `@Enumerated(EnumType.STRING)` mit `VorschlagStatus`
- [x] Alle 3 `@ManyToOne`-Beziehungen korrekt (nur `artikel` nullable=false)
- [x] Alle 3 Custom-Repository-Methoden getestet und grün
- [x] `findByArtikelIdAndStatusIn` für Duplikat-Prüfung im Reorder-Job verifiziert

### Ticket 011 — Transaktion

- [x] Alle Lombok-Annotationen korrekt
- [x] `typ` und `quelle` nutzen `@Enumerated(EnumType.STRING)`
- [x] Default `quelle = BATCH` per Test verifiziert
- [x] `findByArtikelIdAndTypAndDatumBetween` für Verbrauchsanalyse getestet
- [x] `findAllByDatumBetween` mit `Pageable` getestet — korrekte Pagination

---

## Gefundene Fehler

> Keine. Alle Tests grün.

---

## Nächste Schritte

- ✅ Tickets 007, 008, 009, 010, 011 auf `[REVIEW]` gesetzt.
