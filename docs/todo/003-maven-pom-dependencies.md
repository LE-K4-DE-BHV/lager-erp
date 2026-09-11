---
Titel: Maven pom.xml – vollständige Dependencies konfigurieren
Status: [DONE]
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Konfiguriere die `backend/pom.xml` mit allen benötigten Dependencies für das Lager-Management-System. Das bestehende Spring Boot Skeleton (`src/main/java/com/lagermanagement/space/SpaceApplication.java`) muss in die neue `backend/`-Verzeichnisstruktur verschoben werden.

Referenz: Masterplan Unit 2 — R-D1, R_3.1.1

**Zu modifizierende Datei:** `backend/pom.xml`

**Vollständige Dependency-Liste:**

| Dependency | Group/Artifact | Version |
|---|---|---|
| Spring Web | `spring-boot-starter-web` | via BOM |
| Spring Data JPA | `spring-boot-starter-data-jpa` | via BOM |
| Spring Security | `spring-boot-starter-security` | via BOM |
| Spring Validation | `spring-boot-starter-validation` | via BOM |
| Spring Mail | `spring-boot-starter-mail` | via BOM |
| PostgreSQL Driver | `org.postgresql:postgresql` | via BOM |
| Lombok | `org.projectlombok:lombok` | via BOM |
| Apache POI | `org.apache.poi:poi-ooxml` | `5.2.5` |
| OpenCSV | `com.opencsv:opencsv` | `5.9` |
| OpenPDF | `com.github.librepdf:openpdf` | `1.3.43` |
| JAXB API | `jakarta.xml.bind:jakarta.xml.bind-api` | `4.0.2` |
| JAXB Impl | `com.sun.xml.bind:jaxb-impl` | `4.0.5` |
| Spring Test | `spring-boot-starter-test` | via BOM |

**Java-Version:** Java 25 (`<java.version>25</java.version>`)

**Paketname:** `com.lagermanagement.space` (bestehende Struktur beibehalten)

## Akzeptanzkriterien

- [x] Alle 13 Pflicht-Dependencies vorhanden + spring-security-test + Testcontainers (Bonus)
- [x] Java-Version auf 25 gesetzt (`<java.version>25</java.version>` + `<release>25</release>`)
- [x] `SpaceApplication.java` liegt korrekt unter `backend/src/main/java/com/lagermanagement/space/`
- [x] Lombok Annotation-Processor im `maven-compiler-plugin` konfiguriert
- [x] BOM-verwaltete Dependencies ohne fest codierte Version
- [x] `mvn package -DskipTests` laeuft ohne Fehler (BUILD SUCCESS, 2026-04-20)

Implementierung abgeschlossen.
---



---
**Doku erstellt (2026-04-28)**: docs/dev/setup-projektstruktur.md
