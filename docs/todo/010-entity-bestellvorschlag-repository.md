---
Titel: Entity Bestellvorschlag + BestellvorschlagRepository
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle die JPA-Entity `Bestellvorschlag` und das zugehörige Repository. `Bestellvorschlag` bildet den gesamten Lebenszyklus eines Reorder-Vorschlags ab: `VORSCHLAG` → `BESTELLT` → `GELIEFERT` (oder `IGNORIERT`).

Referenz: Masterplan Unit 3 — R-L1, R-L2, R-L3, Tabelle `bestellvorschlaege` in `schema.sql`

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/domain/entity/Bestellvorschlag.java`
- `backend/src/main/java/com/lagermanagement/space/domain/repository/BestellvorschlagRepository.java`

**Entity-Felder (gemäß schema.sql):**
- `id` (BIGSERIAL, PK)
- `artikel` (`@ManyToOne`, FK zu `artikel.id`, NOT NULL)
- `lieferant` (`@ManyToOne`, FK zu `lieferanten.id`, optional)
- `bestellung` (`@ManyToOne`, FK zu `bestellungen.id`, optional — wird gesetzt wenn Status `BESTELLT`)
- `bestandBeiErstellung` (INTEGER, NOT NULL)
- `vorgeschlageneMenge` (INTEGER, NOT NULL)
- `status` (`VorschlagStatus`, `@Enumerated(EnumType.STRING)`, DEFAULT `VORSCHLAG`)
- `erstelltAm` (TIMESTAMP, `@CreatedDate`)
- `aktualisiertAm` (TIMESTAMP, `@LastModifiedDate`)

**Repository-Methoden (Custom Queries):**
```
findByArtikelIdAndStatusIn(Long artikelId, List<VorschlagStatus> statuses)
findAllByStatusIn(List<VorschlagStatus> statuses)
findTopByArtikelIdAndStatusOrderByErstelltAmDesc(Long artikelId, VorschlagStatus status)
```

## Implementierungsnotiz

Implementierung abgeschlossen. `mvn compile` → BUILD SUCCESS.
Erstellte Dateien:
- `domain/entity/Bestellvorschlag.java`
- `domain/repository/BestellvorschlagRepository.java`

## Akzeptanzkriterien

- [ ] `Bestellvorschlag.java` nutzt Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- [ ] `status` nutzt `@Enumerated(EnumType.STRING)` mit Typ `VorschlagStatus`
- [ ] Alle 3 `@ManyToOne`-Beziehungen sind korrekt definiert (alle nullable außer `artikel`)
- [ ] Alle 3 Custom-Repository-Methoden sind deklariert
- [ ] `findByArtikelIdAndStatusIn` ermöglicht die Duplikat-Prüfung im Reorder-Job
---


---
**Doku erstellt (2026-04-28)**: docs/dev/api-bestellung.md
