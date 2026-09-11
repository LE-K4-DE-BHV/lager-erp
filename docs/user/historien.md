# Transaktionshistorie

**Ziel**: Du analysierst alle gebuchten Warenbewegungen und filterst nach Artikel oder Zeitraum.

## Voraussetzungen

- Du bist am System angemeldet.
- Lagerbewegungen wurden entweder manuell gebucht oder per CSV/Excel-Import importiert.

## Zur Transaktionshistorie navigieren

1. Klicke in der Navigation auf **Transaktionen**.

Du siehst eine paginierte Tabelle aller Warenbewegungen, sortiert nach Datum (neueste zuerst).

## Filtern

Die Tabelle lässt sich einschränken:

| Filter | Beschreibung |
|---|---|
| **Artikel** | Zeigt nur Transaktionen für einen bestimmten Artikel |
| **Von – Bis** | Zeitraum (Datum) eingrenzen |

Klicke nach Eingabe der Filterkriterien auf **Suchen**. Klicke auf **Zurücksetzen**, um alle Filter zu löschen.

## Tabellenfelder

| Feld | Beschreibung |
|---|---|
| **Datum** | Buchungsdatum der Transaktion |
| **Artikelnummer** | Artikel-Kennung |
| **Bezeichnung** | Artikelname |
| **Typ** | `EINGANG` oder `AUSGANG` |
| **Buchungstyp** | Nur bei Ausgängen: z.B. `VERBRAUCH`, `ABSCHREIBUNG` |
| **Menge** | Gebuchte Stückzahl |
| **Quelle** | `MANUELL` (manuell gebucht) oder `BATCH` (aus Import) |
| **Benutzer** | Nur bei manuellen Buchungen: `operator` |
| **Bestellnummer** | Nur bei bestellungsbezogenen Eingängen |

## Paginierung

Die Tabelle zeigt standardmäßig 50 Einträge pro Seite. Nutze die Seitenwechsel-Schaltflächen am unteren Tabellenrand, um weitere Einträge anzuzeigen.

## Fehlerbehebung

### Problem: Transaktionen fehlen, obwohl Import-Dateien verarbeitet wurden
- **Ursache**: Der Import hat einzelne fehlerhafte Zeilen übersprungen.
- **Lösung**: Prüfe den Ordner `files/Processed/Transaktionen/Warning/` – dort liegen Dateien, die mit Warnungen verarbeitet wurden. Die übersprungenen Zeilen sind im `fehler_details`-Feld des `import_log`-Eintrags dokumentiert.
