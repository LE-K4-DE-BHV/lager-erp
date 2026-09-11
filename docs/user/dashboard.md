# Dashboard

**Ziel**: Du hast einen schnellen Überblick über den Lagerstatus und bearbeitest offene Bestellvorschläge direkt von der Startseite aus.

## Voraussetzungen

- Du bist am System angemeldet (siehe [Anmelden und Abmelden](login.md)).

## Aufbau des Dashboards

Das Dashboard ist die Startseite nach dem Login und zeigt drei Bereiche:

### 1. KPI-Kacheln

Die oberen Kacheln zeigen auf einen Blick:

| Kennzahl | Bedeutung |
|---|---|
| **Offene Vorschläge** | Anzahl der Bestellvorschläge, die noch nicht bearbeitet wurden |
| **Kritische Vorschläge** | Vorschläge für Artikel, deren Bestand den Sicherheitsbestand unterschritten hat |
| **Letzter Analyse-Lauf** | Zeitpunkt der letzten automatischen Reorder-Analyse |

### 2. Bestellvorschläge-Tabelle

Die Tabelle listet alle offenen Bestellvorschläge auf. Kritische Vorschläge (Bestand ≤ Sicherheitsbestand) sind farblich hervorgehoben.

Jede Zeile zeigt:
- Artikelnummer und Bezeichnung
- Lieferantenname
- Aktueller Bestand / Bestellpunkt / Sicherheitsbestand
- Vorgeschlagene Bestellmenge

### 3. Aktionsschaltflächen

Pro Vorschlag gibt es drei Aktionen:

| Schaltfläche | Aktion |
|---|---|
| **Schnellbestellen** | Bestellung mit der vorgeschlagenen Menge und dem Standardpreis direkt aufgeben |
| **Bearbeiten & Bestellen** | Bestellformular öffnen – Menge, Preis und Lieferdatum anpassen |
| **Ignorieren** | Vorschlag als ignoriert markieren (verschwindet aus der Liste) |

## Bestellung aufgeben (Schnellbestellung)

1. Suche den gewünschten Vorschlag in der Tabelle.
2. Klicke auf **Schnellbestellen**.

**Ergebnis**: Die Bestellung wird mit der vorgeschlagenen Menge und dem Standardeinkaufspreis sofort angelegt. Das System generiert automatisch eine PDF-Bestelldatei unter `files/Output/Orders/`. Der Vorschlagsstatus wechselt auf `BESTELLT`.

## Bestellung aufgeben (bearbeitet)

1. Suche den gewünschten Vorschlag in der Tabelle.
2. Klicke auf **Bearbeiten & Bestellen**.
3. Passe im Bestellformular an:
   - **Bestellmenge**: gewünschte Menge (mindestens 1)
   - **Einkaufspreis**: tatsächlich vereinbarter Preis
   - **Gewünschtes Lieferdatum**: optionales Zieldatum
   - **Notiz**: interne Bemerkung zur Bestellung
4. Klicke auf **Bestellen**.

**Ergebnis**: Gleich wie Schnellbestellung, aber mit den von dir eingegebenen Werten.

## Vorschlag ignorieren

1. Suche den Vorschlag in der Tabelle.
2. Klicke auf **Ignorieren**.

**Ergebnis**: Der Vorschlag verschwindet aus der Liste. Fällt der Bestand später unter den Sicherheitsbestand, erstellt das System beim nächsten Analyse-Lauf automatisch einen neuen Vorschlag.

## Reorder-Analyse manuell starten

Klicke auf **Analyse starten** (oder den entsprechenden Button im Dashboard), um die Bestandsanalyse außerhalb des geplanten Nachtlaufs (02:00 Uhr) zu starten.

**Ergebnis**: Neue Vorschläge erscheinen sofort in der Tabelle.

## Fehlerbehebung

### Problem: Dashboard zeigt keine Vorschläge, obwohl Artikel unter Bestellpunkt
- **Ursache**: Die Reorder-Analyse hat noch nicht gelaufen.
- **Lösung**: Klicke auf **Analyse starten**, um die Analyse manuell auszulösen.

### Problem: Kritischer Vorschlag erscheint erneut nach Ignorieren
- **Ursache**: Der Bestand ist unter den Sicherheitsbestand gefallen – das System stuft den Artikel als kritisch ein.
- **Lösung**: Buche einen Wareneingang (siehe [Lagerbewegungen](lagerbewegungen.md)) oder gib die Bestellung auf.
