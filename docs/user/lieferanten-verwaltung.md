# Lieferantenverwaltung

**Ziel**: Du siehst alle Lieferanten im System und aktualisierst deren Kontaktdaten und Lieferzeit.

## Voraussetzungen

- Du bist am System angemeldet.
- Lieferanten werden primär über den automatischen CSV/Excel-Import angelegt (nicht manuell über das Frontend).

## Zur Lieferantenübersicht navigieren

1. Klicke in der Navigation auf **Lieferanten**.

Du siehst eine Tabelle aller importierten Lieferanten mit Kontaktdaten und Lieferzeit.

## Lieferant bearbeiten

Die Geschäftsnummer (`lieferant_id`, z.B. `LIF-001`) ist unveränderlich. Du kannst Kontaktdaten und Lieferzeit aktualisieren:

1. Klicke in der Tabelle auf **Bearbeiten** neben dem gewünschten Lieferanten.
2. Passe die Felder an:

| Feld | Pflicht | Beschreibung |
|---|---|---|
| **Name** | Ja | Voller Firmenname, max. 200 Zeichen |
| **Kontakt-E-Mail** | Nein | E-Mail-Adresse für Bestellungen |
| **Kontakttelefon** | Nein | Telefonnummer |
| **Lieferzeit (Tage)** | Ja | Durchschnittliche Lieferzeit in Werktagen (mindestens 1) |

3. Klicke auf **Speichern**.

**Ergebnis**: Die Kontaktdaten werden aktualisiert. Die Lieferzeit wirkt sich auf zukünftige Dispositionsberechnungen aus.

## Neue Lieferanten importieren

Neue Lieferanten legst du über den automatischen Datei-Import an:

1. Erstelle eine CSV- oder Excel-Datei mit den Lieferantendaten:
   - Pflichtfelder: `lieferant_id`, `name`, `lead_time_tage`
   - Optionale Felder: `kontakt_email`, `kontakt_telefon`
2. Lege die Datei in den Ordner `files/Input/Stammdaten/`.
3. Der Import-Scheduler verarbeitet die Datei automatisch innerhalb von 15 Minuten.

**Ergebnis**: Neue Lieferanten erscheinen in der Übersicht. Bereits vorhandene Lieferanten (gleiche `lieferant_id`) werden aktualisiert.

## Fehlerbehebung

### Problem: Importierte Lieferanten erscheinen nicht in der Übersicht
- **Ursache 1**: Der Import-Scheduler hat die Datei noch nicht verarbeitet (läuft alle 15 Minuten).
- **Lösung**: Warte bis zu 15 Minuten oder prüfe den `import_log` in der Datenbank.
- **Ursache 2**: Die Importdatei enthält Fehler.
- **Lösung**: Prüfe den Ordner `files/Processed/Stammdaten/Error/` – dort liegen Dateien, die nicht verarbeitet werden konnten.
