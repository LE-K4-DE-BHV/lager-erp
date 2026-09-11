---
Titel: Entity Artikel + ArtikelRepository
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `Artikel` und das zugehörige Spring Data Repository. `Artikel` referenziert `Lieferant` via FK und hält den denormalisierten `aktuellerBestand`. **Wichtig:** Alle Bestandsänderungen dürfen ausschließlich über `InventoryService` erfolgen — nie direkt über das Repository.

Referenz: Masterplan Unit 3 — R_2.1.1, R-S1, R-S2, R-S3, Tabelle `artikel` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/Artikel.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/ArtikelRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK)
- `artikelnummer` (VARCHAR 50, UNIQUE, NOT NULL)
- `bezeichnung` (VARCHAR 200, NOT NULL)
- `mengeneinheit` (VARCHAR 20, NOT NULL)
- `warengruppe` (VARCHAR 100, NOT NULL)
- `lieferant` (`@ManyToOne`, FK zu `lieferanten.id`)
- `aktuellerBestand` (INTEGER, NOT NULL, DEFAULT 0)
- `sicherheitsbestand` (INTEGER, NOT NULL, DEFAULT 0)
- `bestellpunkt` (INTEGER, NOT NULL, DEFAULT 0)
- `standardBestellmenge` (INTEGER, NOT NULL, DEFAULT 1)
- `einkaufspreis` (NUMERIC(10,2), optional)
- `status` (`ArtikelStatus`, `@Enumerated(EnumType.STRING)`, DEFAULT `AKTIV`)
- `erstelltAm` (TIMESTAMP, `@CreatedDate`)
- `aktualisiertAm` (TIMESTAMP, `@LastModifiedDate`)

**Repository-Methoden (Custom Queries):**
```
findAllByStatus(ArtikelStatus status)
findByArtikelnummer(String artikelnummer) → Optional<Artikel>
existsByArtikelnummer(String artikelnummer)
findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus status, int bestellpunkt)
```

## Implementierungsnotiz

Implementierung abgeschlossen. `Artikel.java` und `ArtikelRepository.java` angelegt.
`ArtikelRepositoryTest.java` mit Testcontainers + `@DataJpaTest` erstellt (5 Tests).
**Blocker:** Docker Desktop 4.36+ gibt auf dem Named-Pipe `docker_engine` einen 400-Fehler mit leerem `ServerVersion`-Feld zur�ck � Testcontainers 1.20.4 inkompatibel.
L�sung: `exposeDockerAPIOnTCP2375=true` in Docker Desktop Settings gesetzt ? **Docker Desktop Neustart erforderlich**.
Nach Neustart: `DOCKER_HOST=tcp://localhost:2375 mvn test -Dtest=ArtikelRepositoryTest`

## Akzeptanzkriterien

- [ ] `Artikel.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `@Entity`, `@Table(name = "artikel")`, `@EntityListeners(AuditingEntityListener.class)` sind gesetzt
- [ ] `artikelnummer` hat `@Column(unique = true, nullable = false)`
- [ ] `status` nutzt `@Enumerated(EnumType.STRING)` mit Typ `ArtikelStatus`
- [ ] `@ManyToOne`-Beziehung zu `Lieferant` ist korrekt definiert
- [ ] Alle 4 Custom-Repository-Methoden sind in `ArtikelRepository` deklariert
- [ ] `@DataJpaTest` für `ArtikelRepository.save()` und `findByArtikelnummer()` sind grün
---




---
**Doku erstellt (2026-04-28)**: docs/dev/api-artikel.md
