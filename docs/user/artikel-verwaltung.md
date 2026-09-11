# Artikelverwaltung

**Ziel**: Du legst neue Artikel an, aktualisierst Stammdaten und deaktivierst Artikel, die nicht mehr im Sortiment sind.

## Voraussetzungen

- Du bist am System angemeldet.
- Lieferanten sind bereits im System vorhanden (per Import oder manuell in der Datenbank).

## Zur Artikelübersicht navigieren

1. Klicke in der Navigation auf **Artikel**.

Du siehst eine Tabelle aller Artikel (aktive und inaktive) mit Bestand, Bestellpunkt und Status.

## Neuen Artikel anlegen

1. Klicke auf **Neuer Artikel**.
2. Fülle das Formular aus:

| Feld | Pflicht | Beschreibung |
|---|---|---|
| **Artikelnummer** | Ja | Eindeutige Kennung, max. 50 Zeichen (z.B. `ART-001`) |
| **Bezeichnung** | Ja | Klarer Name des Artikels, max. 200 Zeichen |
| **Mengeneinheit** | Ja | z.B. `Stk`, `kg`, `l`, `m` |
| **Warengruppe** | Ja | z.B. `Befestigungsmaterial`, `Elektronik` |
| **Lieferant** | Nein | Zuordnung zum Lieferanten (aus der Dropdown-Liste) |
| **Sicherheitsbestand** | Ja | Mindestbestand – Unterschreitung löst kritischen Vorschlag aus |
| **Bestellpunkt** | Ja | Bestand, bei dem ein Bestellvorschlag erstellt wird |
| **Standardbestellmenge** | Ja | Menge, die bei der Schnellbestellung vorgeschlagen wird |
| **Einkaufspreis** | Ja | Standardpreis in Euro (> 0) |

3. Klicke auf **Speichern**.

**Ergebnis**: Der Artikel wird mit dem Status `AKTIV` und einem Anfangsbestand von 0 angelegt. Bestand kann über [Lagerbewegungen](lagerbewegungen.md) erhöht werden.

## Artikel aktualisieren

1. Klicke in der Tabelle auf den Artikel, den du bearbeiten möchtest.
2. Passe die gewünschten Felder an (Artikelnummer ist nicht änderbar).
3. Klicke auf **Speichern**.

**Ergebnis**: Die Stammdaten werden aktualisiert.

## Artikel deaktivieren

Artikel können nicht gelöscht werden (Datenintegrität / Historienerhalt). Du deaktivierst sie stattdessen.

1. Klicke in der Tabelle neben dem Artikel auf **Deaktivieren**.
2. Bestätige die Aktion.

**Ergebnis**: Der Artikel-Status wechselt auf `INAKTIV`. Deaktivierte Artikel nehmen nicht mehr an der Reorder-Analyse teil und erhalten keine neuen Bestellvorschläge.

## Fehlerbehebung

### Problem: "Artikelnummer bereits vorhanden" beim Anlegen
- **Ursache**: Eine Artikelnummer muss systemweit eindeutig sein. Es existiert bereits ein Artikel mit dieser Nummer.
- **Lösung**: Wähle eine andere Artikelnummer oder prüfe, ob der Artikel bereits existiert (auch inaktive Artikel tauchen in der Tabelle auf).

### Problem: Lieferant erscheint nicht in der Dropdown-Liste
- **Ursache**: Es sind noch keine Lieferanten im System vorhanden.
- **Lösung**: Importiere Lieferanten über die CSV-Import-Funktion (Datei in `files/Input/Stammdaten/` legen) oder lies die [Lieferantenverwaltung](lieferanten-verwaltung.md).
