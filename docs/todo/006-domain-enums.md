---
Titel: Domain-Enums anlegen (ArtikelStatus, VorschlagStatus, TransaktionTyp, TransaktionQuelle)
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Erstelle alle Domain-Enums im Package `com.lagermanagement.space.domain.enums`. Die Enums bilden die Grundlage für die typsichere Status-Verwaltung in den JPA-Entities.

Referenz: Masterplan Unit 3

**Erstellte Dateien:**

- `domain/enums/ArtikelStatus.java` — `AKTIV`, `INAKTIV`
- `domain/enums/VorschlagStatus.java` — `VORSCHLAG`, `BESTELLT`, `GELIEFERT`, `IGNORIERT`
- `domain/enums/TransaktionTyp.java` — `EINGANG`, `AUSGANG`
- `domain/enums/TransaktionQuelle.java` — `BATCH`, `MANUELL`

Alle Enums werden in den Entities mit `@Enumerated(EnumType.STRING)` verwendet.

## Akzeptanzkriterien

- [x] Alle 4 Enums sind im Package `com.lagermanagement.space.domain.enums` angelegt
- [x] Alle Enum-Werte stimmen mit den Status-Strings im `schema.sql` überein (AKTIV, INAKTIV, VORSCHLAG, BATCH...)
- [x] Enums sind als `public enum` ohne weitere Logik implementiert (reine Wert-Enums)
- [x] Keine `@Autowired`-Felder in Enums

Implementierung abgeschlossen. 7 Unit-Tests in `DomainEnumsTest` — alle grün (0 Failures, 0 Errors).
---


---
**Doku erstellt (2026-04-28)**: docs/dev/setup-projektstruktur.md
