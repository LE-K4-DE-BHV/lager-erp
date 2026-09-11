---
Titel: Spring Security – HTTP Basic Auth, Session & CSRF-Konfiguration
Status: [DONE]

## Implementierungsnotiz
Implementierung abgeschlossen. `SecurityConfig` mit HTTP Basic Auth + formLogin, CSRF deaktiviert, `SessionCreationPolicy.IF_REQUIRED`. Login-Erfolg/Fehler liefern JSON (kein HTML). `JpaAuditingConfig` war bereits vorhanden. `SecurityUtils.getCurrentUsername()` liest aus SecurityContextHolder.
Zuweisung: Backend Dev
Priorität: High
---

## Beschreibung

Implementiere die vollständige Spring Security-Konfiguration. Das System nutzt HTTP Basic Auth mit einem einzigen Benutzer (`operator`). Nach erfolgreichem Login stellt Spring Security ein `JSESSIONID`-Cookie aus (`httpOnly=true`). CSRF wird für die REST-SPA-Architektur deaktiviert.

Referenz: Masterplan Unit 4 — R-A1, R-A2, R-A3, R_3.5.1, Sicherheitsregeln

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/config/SecurityConfig.java`
- `backend/src/main/java/com/lagermanagement/space/config/JpaAuditingConfig.java`
- `backend/src/main/java/com/lagermanagement/space/web/utils/SecurityUtils.java`

**SecurityConfig – Kernkonfiguration:**
- `UserDetailsService`: Einzelner User `operator`, BCrypt-Hash aus `${app.security.operator-password-hash}`
- Alle Requests außer `POST /api/v1/auth/login` erfordern Authentifizierung
- CSRF: `http.csrf(csrf -> csrf.disable())`
- Session: `SessionCreationPolicy.IF_REQUIRED` (Standard)
- Login-Erfolg: `200 OK` mit JSON-Body (kein Redirect)
- Login-Fehler: `401 Unauthorized` mit JSON-Body (kein HTML)
- Unauthorized-Handler: `401` als JSON, kein Redirect auf Login-Seite

**JpaAuditingConfig:**
- `@Configuration` + `@EnableJpaAuditing`
- Ermöglicht `@CreatedDate` und `@LastModifiedDate` in Entities

**SecurityUtils:**
```java
public static String getCurrentUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
}
```

**Wichtig:** Kein JWT, kein OAuth, keine komplexen Rollen-Prüfungen (`@PreAuthorize`). BCrypt-Hash NIEMALS im Code hard-coden.

## Akzeptanzkriterien

- [ ] `SecurityConfig` lädt BCrypt-Hash aus Umgebungsvariable `${app.security.operator-password-hash}` (nie Klartext)
- [ ] CSRF ist deaktiviert
- [ ] `SessionCreationPolicy.IF_REQUIRED` ist gesetzt
- [ ] Nach Login enthält die Response ein `JSESSIONID`-Cookie mit `httpOnly=true`
- [ ] Zugriff ohne Session auf `/api/v1/**` gibt `401` als JSON zurück (kein HTML-Redirect)
- [ ] `JpaAuditingConfig` aktiviert `@EnableJpaAuditing`
- [ ] `SecurityUtils.getCurrentUsername()` liefert `"operator"` bei eingeloggter Session
- [ ] Kein `@Autowired` auf Feldern — ausschließlich Constructor-Injection via `@RequiredArgsConstructor`
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-security.md
