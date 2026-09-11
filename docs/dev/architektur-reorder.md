# Architektur: Reorder-Analyse & Berichtserstellung

**Ziel**: Du verstehst, wie das System automatisch Bestellvorschläge erstellt und tägliche Bestandsberichte als PDF generiert.

## Überblick

Das Reorder-System analysiert täglich den Lagerbestand aller aktiven Artikel. Fällt ein Bestand unter den konfigurierten Bestellpunkt, erstellt das System automatisch einen **Bestellvorschlag** (Status: `VORSCHLAG`). Der Operator entscheidet dann im Dashboard, ob er bestellt oder den Vorschlag ignoriert.

## Zeitpläne

Beide Jobs werden über `application.yml` konfiguriert:

```yaml
app:
  reorder:
    consumption-period-days: 30       # Analysezeitraum für Verbrauchsberechnung
    cron-expression: "0 0 2 * * *"    # Täglich 02:00 Uhr – Reorder-Analyse
  report:
    cron-expression: "0 30 6 * * *"   # Täglich 06:30 Uhr – PDF-Bericht
```

## Reorder-Analyse-Logik

Der `ReorderAnalysisService` prüft alle aktiven Artikel mit einem Bestellpunkt > 0:

```
Für jeden aktiven Artikel:
  1. Aktueller Bestand > Bestellpunkt? → KEIN Vorschlag nötig
  2. Aktiver Vorschlag (VORSCHLAG oder BESTELLT) vorhanden? → ÜBERSPRINGEN (Duplikatschutz)
  3. Letzter Vorschlag war IGNORIERT und Bestand > Sicherheitsbestand? → ÜBERSPRINGEN
  4. IGNORIERT aber Bestand ≤ Sicherheitsbestand (kritisch!)? → NEUER VORSCHLAG erzwungen
  5. Kein aktiver Vorschlag → NEUER VORSCHLAG erstellen
```

**Vorgeschlagene Menge**: `standard_bestellmenge` des Artikels (konfiguriert in den Artikelstammdaten).

**Kritischer Vorschlag**: Ein Vorschlag gilt als kritisch (`istKritisch = true`), wenn `aktueller_bestand ≤ sicherheitsbestand`. Kritische Vorschläge werden im Dashboard hervorgehoben.

## Vorschlag-Lifecycle

```
VORSCHLAG ──► BESTELLT    (Operator hat eine Bestellung ausgelöst)
         └──► IGNORIERT   (Operator hat den Vorschlag ignoriert)
```

Ein ignorierter Vorschlag kann durch einen erneuten Analyse-Lauf wieder zu `VORSCHLAG` werden, wenn der Bestand den Sicherheitsbestand unterschreitet.

## Manueller Trigger

Die Reorder-Analyse kann auch manuell aus dem Dashboard heraus angestoßen werden:

```bash
curl -X POST 'http://localhost/api/v1/reorder/trigger' \
  -b 'JSESSIONID=DEIN_SESSION_COOKIE'
```

Response:
```json
{ "message": "Analyse abgeschlossen", "neueVorschlaege": 3 }
```

## PDF-Berichtserstellung (BerichtService)

Täglich um 06:30 Uhr generiert der `ReorderScheduler` via `BerichtService` einen Tagesbericht als PDF-Datei. Die Datei wird im Verzeichnis `files/Output/Reports/` abgelegt.

Der Bericht enthält:
- Alle Artikel mit kritischem Bestand (≤ Sicherheitsbestand)
- Offene Bestellvorschläge
- Offene Bestellungen (Status `OFFEN`)

## PDF-Bestellausgabe (PdfGeneratorService / XmlGeneratorService)

Beim Auslösen einer Bestellung werden zwei Ausgabedateien generiert:

| Format | Service | Ablageort |
|---|---|---|
| PDF | `PdfGeneratorService` (OpenPDF) | `files/Output/Orders/Bestellung_BEST-YYYY-NNN_YYYY-MM-DD.pdf` |
| XML | `XmlGeneratorService` (JAXB) | `files/Output/Orders/Bestellung_BEST-YYYY-NNN_YYYY-MM-DD.xml` |

Die XML-Ausgabe nutzt JAXB-annotierte Klassen (keine Records – JAXB benötigt No-Args-Konstruktoren).

## Fehlerbehebung

### Problem: Kein Vorschlag obwohl Bestand unter Bestellpunkt
- **Ursache**: Für diesen Artikel existiert bereits ein aktiver Vorschlag (`VORSCHLAG` oder `BESTELLT`).
- **Lösung**: Offene Vorschläge im Dashboard bearbeiten (bestellen oder ignorieren), dann Analyse manuell triggern.

### Problem: Ignorierter Vorschlag erscheint wieder
- **Ursache**: Der Bestand ist unter den Sicherheitsbestand gefallen – das System stuft den Artikel als kritisch ein und erstellt einen neuen Vorschlag.
- **Lösung**: Bestand durch einen Warenzugang erhöhen oder die Bestellung aufgeben.
