# Anmelden und Abmelden

**Ziel**: Du meldest dich am Lager-Management-System an und navigierst zur Hauptansicht.

## Voraussetzungen

- Das System läuft (`docker compose up`).
- Du hast die Zugangsdaten: Benutzername `operator` und das vereinbarte Passwort.
- Der Browser ist auf [http://localhost](http://localhost) geöffnet.

## Anmelden

1. Öffne [http://localhost](http://localhost) im Browser.
2. Du siehst die Anmeldemaske. Gib deine Zugangsdaten ein:
   - **Benutzername**: `operator`
   - **Passwort**: dein konfiguriertes Passwort
3. Klicke auf **Anmelden**.

**Ergebnis**: Du wirst automatisch zum Dashboard weitergeleitet. Das System merkt sich deine Session im `JSESSIONID`-Cookie. Du bleibst angemeldet, bis du dich explizit abmeldest oder den Browser schließt.

## Abmelden

1. Klicke oben rechts im Header auf **Abmelden**.

**Ergebnis**: Deine Session wird beendet. Du wirst auf die Anmeldemaske zurückgeleitet.

## Fehlerbehebung

### Problem: "Ungültige Anmeldedaten"
- **Ursache**: Benutzername oder Passwort falsch.
- **Lösung**: Stelle sicher, dass du genau `operator` (Kleinschreibung) als Benutzernamen verwendest. Das Passwort ist das, das in der `.env`-Datei als BCrypt-Hash hinterlegt wurde.
- **Prävention**: Lass die Zugangsdaten vom Administrator in einer sicheren Notiz hinterlegen.

### Problem: Seite lädt nicht – Verbindungsfehler
- **Ursache**: Das System ist nicht gestartet.
- **Lösung**: Öffne ein Terminal und führe im Projektverzeichnis `docker compose up -d` aus. Warte ca. 30 Sekunden, dann lade die Seite neu.

### Problem: Nach Seite neuladen wieder auf Login
- **Ursache**: Der Browser blockiert Cookies von `localhost` oder der Cookie ist abgelaufen.
- **Lösung**: Prüfe die Cookie-Einstellungen des Browsers. Stelle sicher, dass Cookies für `localhost` erlaubt sind.
