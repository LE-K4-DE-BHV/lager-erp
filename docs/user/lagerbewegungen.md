# Lagerbewegungen buchen

**Ziel**: Du buchst manuelle Wareneingänge und -ausgänge und aktualisierst damit den aktuellen Lagerbestand.

## Voraussetzungen

- Du bist am System angemeldet.
- Der Artikel, für den du eine Bewegung buchen möchtest, ist im System angelegt und aktiv.

## Zur Lagerbewegungsansicht navigieren

1. Klicke in der Navigation auf **Lagerbewegung**.

Du siehst zwei Formulare nebeneinander: **Wareneingang** und **Warenausgang**.

## Wareneingang buchen

Verwende den Wareneingang, wenn Ware angeliefert wird (z.B. aus einer Bestellung oder einer Rückgabe).

1. Wähle unter **Wareneingang** den Artikel aus der Dropdown-Liste.
2. Gib die Menge ein (mindestens 1).
3. Wähle das Buchungsdatum (Standard: heutiges Datum).
4. Klicke auf **Eingang buchen**.

**Ergebnis**: Der `aktueller_bestand` des Artikels wird erhöht. Die Buchung wird in der Transaktionshistorie mit `typ: EINGANG` und `quelle: MANUELL` gespeichert. Dein Benutzername (`operator`) wird im Audit-Trail hinterlegt.

### Bestellung gleichzeitig abschließen

Falls eine offene Bestellung für den eingebuchten Artikel vorhanden ist, erscheint nach der Buchung eine Rückfrage:

> „Für diesen Artikel liegt eine offene Bestellung vor. Möchtest du sie jetzt abschließen?"

- **Ja**: Die verknüpfte Bestellung wechselt auf Status `ABGESCHLOSSEN`.
- **Nein**: Die Bestellung bleibt offen.

## Warenausgang buchen

Verwende den Warenausgang, wenn Ware aus dem Lager entnommen wird (z.B. für die Produktion oder Abschreibung).

1. Wähle unter **Warenausgang** den Artikel aus der Dropdown-Liste.
2. Gib die Menge ein (mindestens 1).
3. Wähle das Buchungsdatum.
4. Gib den **Buchungstyp** an (Pflichtfeld), z.B.:
   - `VERBRAUCH` – normale Entnahme für die Produktion
   - `ABSCHREIBUNG` – Verlust, Beschädigung
   - `RETOURE` – Rückgabe an Lieferant
5. Optional: Trage einen **Grund** ein (interne Bemerkung).
6. Klicke auf **Ausgang buchen**.

**Ergebnis**: Der `aktueller_bestand` des Artikels wird reduziert. Die Buchung erscheint in der Transaktionshistorie mit `typ: AUSGANG`.

## Fehlerbehebung

### Problem: "Unzureichender Bestand"
- **Ursache**: Die gebuchte Ausgangsmenge würde den Bestand unter 0 bringen.
- **Lösung**: Reduziere die Menge oder buche zuerst einen Wareneingang.
- **Prävention**: Prüfe den aktuellen Bestand des Artikels vor der Buchung in der Artikelübersicht.

### Problem: Artikel erscheint nicht in der Dropdown-Liste
- **Ursache**: Der Artikel ist deaktiviert oder noch nicht im System angelegt.
- **Lösung**: Lege den Artikel in der [Artikelverwaltung](artikel-verwaltung.md) an oder reaktiviere ihn.
