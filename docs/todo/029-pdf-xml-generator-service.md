---
Titel: PdfGeneratorService + XmlGeneratorService + BestellungXml (JAXB)
Status: [DONE]
Zuweisung: Backend Dev
Priorität: Medium
---

## Beschreibung

Implementiere die Ausgabe-Services für die Bestellgenerierung: PDF via OpenPDF und XML via JAXB. Beide Dateien werden bei jeder Bestellfreigabe in `Output/Orders/` gespeichert. `BestellungXml` ist eine reguläre Klasse mit `@XmlRootElement` (kein Record, da JAXB einen No-Args-Konstruktor benötigt).

Referenz: Masterplan Unit 8 — R-O1, R-O2, R-O3

**Zu erstellende Dateien:**
- `backend/src/main/java/com/lagermanagement/space/service/PdfGeneratorService.java`
- `backend/src/main/java/com/lagermanagement/space/service/XmlGeneratorService.java`
- `backend/src/main/java/com/lagermanagement/space/web/dto/xml/BestellungXml.java`

**PDF-Inhalt (R-O3) — Pflichtfelder:**
- Bestellnummer + Datum
- Lieferant: Name + Kontaktdaten
- Artikeltabelle: Nr., Bezeichnung, Menge, Einheit, Einkaufspreis/Einheit, Teilbetrag
- Gesamtbetrag (Summe aller Teilbeträge)
- Gewünschtes Lieferdatum
- Freitext-Notiz
- Erstellt-von (Benutzer-Audit)
- Firmenlogo-Platzhalter (konfigurierbarer Pfad)

**Dateiname-Schema:** `Bestellung_{BestellNr}_{Datum}.pdf` und `Bestellung_{BestellNr}_{Datum}.xml`

**BestellungXml — JAXB-Klasse (kein Record!):**
- `@XmlRootElement(name = "bestellung")`
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` via Lombok
- Felder entsprechen PDF-Inhalt

**Wichtig:** `BestellungXml` ist eine reguläre Klasse mit `@Data`-Lombok — keine Records, da JAXB einen No-Args-Konstruktor benötigt (Records sind inkompatibel mit JAXB).

## Akzeptanzkriterien

- [ ] `PdfGeneratorService.generate(bestellung)` erstellt eine lesbare PDF-Datei mit allen Pflichtfeldern
- [ ] `XmlGeneratorService.generate(bestellung)` erstellt eine valide XML-Datei
- [ ] Dateiname folgt dem Schema `Bestellung_{Nr}_{Datum}.{pdf|xml}`
- [ ] Beide Dateien werden in `Output/Orders/` gespeichert
- [ ] `BestellungXml` ist eine reguläre `@Data`-Klasse (KEIN Java Record)
- [ ] `BestellungXml` hat `@XmlRootElement` und einen No-Args-Konstruktor
- [ ] Gesamtbetrag im PDF wird korrekt berechnet (`Menge * Einkaufspreis` pro Position)
---


---
**Doku erstellt (2026-04-28)**: docs/dev/architektur-reorder.md
