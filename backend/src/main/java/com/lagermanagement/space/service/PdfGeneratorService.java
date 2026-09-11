package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Bestellung;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final FileService fileService;

    /**
     * Generiert eine Bestellungs-PDF mit allen Pflichtfeldern und speichert sie in Output/Orders/.
     *
     * @return Pfad zur erzeugten PDF-Datei
     */
    public Path generate(Bestellung bestellung) {
        String dateiname = "Bestellung_" + bestellung.getBestellnummer() + "_" + LocalDate.now() + ".pdf";
        Path zielPfad = fileService.resolveOutputPath("Output/Orders").resolve(dateiname);

        try {
            Files.createDirectories(zielPfad.getParent());
        } catch (IOException e) {
            log.error("Ausgabeverzeichnis konnte nicht erstellt werden: {}", e.getMessage());
            throw new RuntimeException("PDF-Ausgabeverzeichnis nicht erstellbar", e);
        }

        Document dokument = new Document(PageSize.A4);
        try (OutputStream out = Files.newOutputStream(zielPfad)) {
            PdfWriter.getInstance(dokument, out);
            dokument.open();

            Font titelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sektionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font kopfFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font zellenFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Titel
            dokument.add(new Paragraph("BESTELLFORMULAR", titelFont));
            dokument.add(new Paragraph(" "));

            // Bestellinformationen
            dokument.add(new Paragraph("Bestellnummer: " + bestellung.getBestellnummer(), sektionFont));
            dokument.add(new Paragraph("Datum: " + LocalDate.now(), textFont));
            if (bestellung.getGewuenschtesLieferdatum() != null) {
                dokument.add(new Paragraph("Gewünschtes Lieferdatum: " + bestellung.getGewuenschtesLieferdatum(), textFont));
            }
            dokument.add(new Paragraph("Erstellt von: " + bestellung.getErstelltVon(), textFont));
            dokument.add(new Paragraph(" "));

            // Lieferant
            dokument.add(new Paragraph("Lieferant", sektionFont));
            var lieferant = bestellung.getLieferant();
            if (lieferant != null) {
                dokument.add(new Paragraph("Name: " + lieferant.getName(), textFont));
                if (lieferant.getKontaktEmail() != null) {
                    dokument.add(new Paragraph("E-Mail: " + lieferant.getKontaktEmail(), textFont));
                }
                if (lieferant.getKontaktTelefon() != null) {
                    dokument.add(new Paragraph("Telefon: " + lieferant.getKontaktTelefon(), textFont));
                }
            }
            dokument.add(new Paragraph(" "));

            // Artikeltabelle
            dokument.add(new Paragraph("Bestellpositionen", sektionFont));
            dokument.add(new Paragraph(" "));

            PdfPTable tabelle = new PdfPTable(6);
            tabelle.setWidthPercentage(100);
            tabelle.setWidths(new float[]{2f, 3.5f, 1.5f, 1.5f, 2f, 2f});

            String[] spalten = {"Artikelnummer", "Bezeichnung", "Menge", "Einheit", "EK-Preis/Einheit", "Teilbetrag"};
            for (String spalte : spalten) {
                PdfPCell zelle = new PdfPCell(new Phrase(spalte, kopfFont));
                zelle.setBackgroundColor(new Color(50, 80, 120));
                zelle.setPadding(5);
                tabelle.addCell(zelle);
            }

            var artikel = bestellung.getArtikel();
            BigDecimal gesamtbetrag = BigDecimal.ZERO;
            if (artikel != null) {
                BigDecimal einkaufspreis = bestellung.getEinkaufspreis() != null
                        ? bestellung.getEinkaufspreis()
                        : BigDecimal.ZERO;
                BigDecimal teilbetrag = einkaufspreis.multiply(BigDecimal.valueOf(bestellung.getBestellmenge()));
                gesamtbetrag = teilbetrag;

                addCell(tabelle, artikel.getArtikelnummer(), zellenFont);
                addCell(tabelle, artikel.getBezeichnung(), zellenFont);
                addCell(tabelle, String.valueOf(bestellung.getBestellmenge()), zellenFont);
                addCell(tabelle, artikel.getMengeneinheit(), zellenFont);
                addCell(tabelle, String.format("%.2f EUR", einkaufspreis), zellenFont);
                addCell(tabelle, String.format("%.2f EUR", teilbetrag), zellenFont);
            }

            dokument.add(tabelle);
            dokument.add(new Paragraph(" "));

            // Gesamtbetrag
            Font gesamtFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            dokument.add(new Paragraph("Gesamtbetrag: " + String.format("%.2f EUR", gesamtbetrag), gesamtFont));
            dokument.add(new Paragraph(" "));

            // Notiz
            if (bestellung.getNotiz() != null && !bestellung.getNotiz().isBlank()) {
                dokument.add(new Paragraph("Notiz", sektionFont));
                dokument.add(new Paragraph(bestellung.getNotiz(), textFont));
            }

            log.info("PDF erstellt: {}", dateiname);

        } catch (Exception e) {
            log.error("Fehler beim Erstellen der Bestellungs-PDF: {}", e.getMessage());
            throw new RuntimeException("PDF-Erstellung fehlgeschlagen: " + e.getMessage(), e);
        } finally {
            if (dokument.isOpen()) {
                dokument.close();
            }
        }

        return zielPfad;
    }

    private void addCell(PdfPTable tabelle, String text, Font font) {
        PdfPCell zelle = new PdfPCell(new Phrase(text != null ? text : "", font));
        zelle.setPadding(4);
        tabelle.addCell(zelle);
    }
}
