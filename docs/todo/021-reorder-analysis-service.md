---
Titel: ReorderAnalysisService – Bestellvorschlag-Logik
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere den `ReorderAnalysisService`, der für jeden aktiven Artikel prüft, ob ein Bestellvorschlag nötig ist. Die Analyse ist synchron aufrufbar (manueller Trigger via REST) und läuft auch automatisch per `@Scheduled`-Job. Die IGNORIERT-Logik muss korrekt implementiert werden.

Referenz: Masterplan Unit 6 — R_2.3.1, R_2.3.2, R_2.3.3, R-L3, R-P4

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/ReorderAnalysisService.java`
- `backend/src/test/java/com/lagermanagement/space/service/ReorderAnalysisServiceTest.java`

**Analyse-Algorithmus (`runAnalysis()`):**
```
Für jeden AKTIVEN Artikel (status=AKTIV, bestellpunkt > 0):
  1. Verbrauch berechnen:
     → Lade AUSGANG-Transaktionen der letzten N Tage (N = consumption-period-days)
     → tagesverbrauch = sum(menge) / N

  2. Prüfe Bedingung: aktueller_bestand <= bestellpunkt?

  3. Duplikat/Ignoriert-Prüfung:
     → VORSCHLAG oder BESTELLT existiert → überspringen
     → IGNORIERT existiert UND bestand > sicherheitsbestand → überspringen
     → IGNORIERT existiert UND bestand <= sicherheitsbestand → NEUER Vorschlag (kritisch!)

  4. Erstelle Bestellvorschlag:
     → vorgeschlageneMenge = artikel.standardBestellmenge
     → bestandBeiErstellung = aktueller_bestand
```

**Rückgabe:** Anzahl neu erstellter Vorschläge (für den manuellen Trigger-Endpunkt).

## Akzeptanzkriterien

- [ ] `runAnalysis()` gibt Anzahl neu erstellter Vorschläge zurück
- [ ] Artikel mit `bestand <= bestellpunkt` bekommen einen Vorschlag
- [ ] Artikel mit `bestand > bestellpunkt` bekommen KEINEN Vorschlag
- [ ] Duplikat-Prüfung: kein zweiter Vorschlag wenn bereits `VORSCHLAG` oder `BESTELLT` aktiv
- [ ] IGNORIERT-Logik: kein neuer Vorschlag wenn `bestand > sicherheitsbestand`
- [ ] IGNORIERT-Logik überschrieben: Vorschlag trotzdem wenn `bestand <= sicherheitsbestand` (kritisch)
- [ ] `consumption-period-days` kommt aus `application.yml` (konfigurierbar)
- [ ] Alle Testszenarien (happy, duplikat, ignoriert-normal, ignoriert-kritisch) sind grün
---




---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-reorder.md
