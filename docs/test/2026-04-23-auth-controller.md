# Testergebnis: AuthController – Login, Logout & Me-Endpunkt

**Datum**: 2026-04-23
**Ticket**: docs/todo/014-auth-controller.md
**Status**: ❌ ROT

## Getestete Klassen / Komponenten

- `AuthController.java` — `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`
- `AuthControllerTest.java` — `@WebMvcTest` + eigene `TestSecurityConfig`

## Testlauf-Zusammenfassung

| Test                                          | Ergebnis       |
|-----------------------------------------------|----------------|
| `shouldReturn200WithUsernameOnSuccessfulLogin` | ❌ FEHLSCHLAG  |
| `shouldReturn401WhenLoginWithWrongPassword`   | ✅ GRÜN        |
| `shouldReturn401WhenMeWithoutAuthentication`  | ✅ GRÜN        |
| `shouldReturnUsernameOnMeWithAuthenticatedSession` | ❌ FEHLSCHLAG |
| `shouldInvalidateSessionOnLogout`             | ❌ FEHLSCHLAG  |
| `shouldReturn401OnMeAfterLogout`              | ✅ GRÜN        |
| `shouldAuthenticateViaFormLogin`              | ✅ GRÜN        |
| `shouldNotAuthenticateWithWrongCredentials`   | ✅ GRÜN        |

**Ergebnis: 5 GRÜN / 3 ROT**

## Gefundene Fehler

### FEHLER-001: Kein JSESSIONID-Cookie bei erfolgreichem Login

**Test**: `shouldReturn200WithUsernameOnSuccessfulLogin:111`
**Meldung**: `No cookie with name 'JSESSIONID'`

**Beschreibung**: Der Login über `POST /api/v1/auth/login` mit korrekten Credentials gibt Status 200 und den korrekten JSON-Body `{"username":"operator"}` zurück. Allerdings enthält die MockMvc-Response keinen `JSESSIONID`-Cookie (`Cookies = []`).

**Relevanz**: Das Akzeptanzkriterium "Nach Login enthält die Response ein JSESSIONID-Cookie mit httpOnly=true" (Ticket 013, Punkt 4) ist nicht erfüllt. Im MockMvc-Umfeld wird kein `Set-Cookie`-Header für die Session gesetzt, obwohl `SessionCreationPolicy.IF_REQUIRED` konfiguriert ist.

**Reproduzierbar mit**:
```java
mockMvc.perform(post("/api/v1/auth/login")
    .param("username", "operator")
    .param("password", "test"))
    .andExpect(cookie().exists("JSESSIONID")); // schlägt fehl
```

---

### FEHLER-002: GET /api/v1/auth/me liefert 404 statt 200

**Test**: `shouldReturnUsernameOnMeWithAuthenticatedSession:135`
**Meldung**: `Status expected:<200> but was:<404>`

**Beschreibung**: Beim Aufruf von `GET /api/v1/auth/me` mit einem via `SecurityMockMvcRequestPostProcessors.user("operator").roles("OPERATOR")` gesetzten Mock-Nutzer gibt MockMvc `404` zurück. Der Handler im Dispatcher-Servlet ist `ResourceHttpRequestHandler` (statische Ressourcen), nicht der `AuthController`.

**Root Cause**: Im `@WebMvcTest(AuthController.class)`-Kontext wird das Request-Mapping des `AuthController` für die Anfragen mit dem `user()` Post-Processor nicht gefunden. Der `AuthController` ist korrekt implementiert (`@RequestMapping("/api/v1/auth")`, `@GetMapping("/me")`), aber die Kombination aus `@WebMvcTest`, `@Import(TestSecurityConfig.class)` und `SecurityMockMvcRequestPostProcessors.user()` verursacht, dass der Controller-Handler nicht aufgelöst wird.

**Beobachtung aus MockMvc-Ausgabe**:
```
Handler:
    Type = org.springframework.web.servlet.resource.ResourceHttpRequestHandler
Resolved Exception:
    Type = NoResourceFoundException
Error message = No static resource api/v1/auth/me.
```

---

### FEHLER-003: POST /api/v1/auth/logout liefert 404 statt 204

**Test**: `shouldInvalidateSessionOnLogout:143`
**Meldung**: `Status expected:<204> but was:<404>`

**Beschreibung**: Identisches Problem wie FEHLER-002. `POST /api/v1/auth/logout` mit Mock-User liefert `404`, da der `AuthController`-Handler vom Dispatcher-Servlet nicht gefunden wird.

---

## Ursachenanalyse

Alle 3 Fehler teilen eine gemeinsame Ursache: der `@WebMvcTest`-Kontext in Kombination mit `@Import(TestSecurityConfig.class)` und `SecurityMockMvcRequestPostProcessors.user()` löst den `AuthController` als Request-Handler nicht auf.

**Beobachtung**: Tests, die den `formLogin()` Request-Builder oder keine Authentifizierung verwenden, funktionieren korrekt (5/8 Tests grün). Tests, die `SecurityMockMvcRequestPostProcessors.user("operator").roles("OPERATOR")` verwenden, scheitern (3/8 Tests rot).

**Empfehlung für den Dev**:
1. Prüfen, ob ein `@TestConfiguration`-Konflikt zwischen der Auto-Konfiguration von `@WebMvcTest` und der `TestSecurityConfig` vorliegt.
2. Alternativ: Verwendung von `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` für Integration-Tests der Auth-Endpunkte.
3. Für den JSESSIONID-Cookie: In echten Servlet-Containern wird der Cookie gesetzt — prüfen, ob `MockMvc` hier eine Einschränkung hat oder ob die Session-Erstellung konfiguriert werden muss.

## Nächste Schritte

- ❌ Ticket auf [IN_PROGRESS] zurückgesetzt.
- Fehler dokumentiert in `docs/test/2026-04-23-auth-controller.md` (diese Datei).
- Nachricht an Dev: Bitte FEHLER-001, FEHLER-002, FEHLER-003 beheben.
