---
Titel: BerichtService – täglicher Bestandsbericht als PDF
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `BerichtService`, der täglich automatisch einen Bestandsbericht als PDF generiert und in `Output/Reports/` ablegt. Das PDF wird mit OpenPDF erstellt und enthält eine Tabelle aller aktiven Artikel mit Bestandsdaten und Status-Ampel.

Referenz: Masterplan Unit 6 — R-P5, Abschnitt "BerichtService.generateDailyReport()"

**Zu erstellende Datei:**
- `backend/src/main/java/com/lagermanagement/space/service/BerichtService.java`

**Bericht-Inhalt (PDF-Tabelle):**

| Spalte | Beschreibung |
|---|---|
| Artikelnummer | Eindeutige Nummer |
| Bezeichnung | Artikelname |
| Einheit | Mengeneinheit |
| Bestand | Aktueller Bestand |
| Bestellpunkt | Reorder-Point |
| Sicherheitsbestand | Safety Stock |
| Status | `Normal` oder `Kritisch` (wenn `aktueller_bestand <= sicherheitsbestand`) |

**Dateiname:** `Bestandsbericht_YYYY-MM-DD.pdf`
**Speicherpfad:** `Output/Reports/Bestandsbericht_YYYY-MM-DD.pdf`

**OpenPDF-Nutzung:** `com.github.librepdf:openpdf` — Tabelle mit `PdfPTable`, Kopfzeile, einfaches Layout.

## Akzeptanzkriterien

- [ ] `generateDailyReport()` erstellt eine PDF-Datei im Pfad `Output/Reports/Bestandsbericht_YYYY-MM-DD.pdf`
- [ ] PDF enthält alle 7 Spalten mit korrekten Daten
- [ ] Status-Feld zeigt `Kritisch` wenn `aktueller_bestand <= sicherheitsbestand`
- [ ] Nur AKTIVE Artikel sind im Bericht enthalten
- [ ] `FileService.resolveOutputPath()` wird für den Ausgabepfad genutzt
- [ ] Fehler beim PDF-Schreiben werden als ERROR geloggt (kein unkontrollierter Absturz)
---




---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-reorder.md
