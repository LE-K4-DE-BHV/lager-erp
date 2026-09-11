# Bestellhistorie

**Ziel**: Du siehst alle aufgegebenen Bestellungen und deren aktuellen Status.

## Voraussetzungen

- Du bist am System angemeldet.
- Bestellungen wurden über das Dashboard aufgegeben.

## Zur Bestellhistorie navigieren

1. Klicke in der Navigation auf **Bestellungen**.

Du siehst eine Tabelle aller Bestellungen, sortiert nach Erstelldatum (neueste zuerst).

## Tabellenfelder

| Feld | Beschreibung |
|---|---|
| **Bestellnummer** | Eindeutige Kennung der Bestellung (Format: `BEST-JJJJ-NNN`) |
| **Artikel** | Artikelnummer der bestellten Ware |
| **Lieferant** | Name des Lieferanten |
| **Bestellmenge** | Bestellte Stückzahl |
| **Einkaufspreis** | Vereinbarter Einkaufspreis in Euro |
| **Lieferdatum** | Gewünschtes Lieferdatum (optional) |
| **Status** | Aktueller Bestellstatus |
| **Erstellt von** | Benutzername des Erstellers (`operator`) |
| **Erstellt am** | Datum und Uhrzeit der Bestellung |

## Bestellstatus

| Status | Bedeutung |
|---|---|
| `OFFEN` | Bestellung wurde aufgegeben, Ware noch nicht eingegangen |
| `ABGESCHLOSSEN` | Wareneingang wurde gebucht und der Bestellung zugeordnet |
| `STORNIERT` | Bestellung wurde storniert |

## Bestelldokumente

Beim Aufgeben einer Bestellung werden automatisch zwei Dokumente generiert:

- **PDF**: `files/Output/Orders/Bestellung_[Bestellnummer]_[Datum].pdf`
- **XML**: `files/Output/Orders/Bestellung_[Bestellnummer]_[Datum].xml`

Diese Dateien findest du im gemounteten `files`-Verzeichnis auf dem Host-System.

## Fehlerbehebung

### Problem: Bestelldokument (PDF) nicht vorhanden
- **Ursache**: Die PDF-Generierung ist beim Anlegen der Bestellung fehlgeschlagen.
- **Lösung**: Prüfe die Backend-Logs (`docker compose logs backend`) auf Fehlermeldungen zur PDF-Generierung. Stelle sicher, dass der Ordner `files/Output/Orders/` existiert und beschreibbar ist.
