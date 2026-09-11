package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BerichtService {

    private final ArtikelRepository artikelRepository;
    private final FileService fileService;

    /**
     * Generiert den täglichen Bestandsbericht als PDF und speichert ihn in Output/Reports/.
     */
    public void generateDailyReport() {
        String dateiname = "Bestandsbericht_" + LocalDate.now() + ".pdf";
        Path zielPfad = fileService.resolveOutputPath("Output/Reports").resolve(dateiname);

        try {
            Files.createDirectories(zielPfad.getParent());
        } catch (IOException e) {
            log.error("Ausgabeverzeichnis konnte nicht erstellt werden: {}", e.getMessage());
            return;
        }

        var artikel = artikelRepository.findAllByStatus(ArtikelStatus.AKTIV);

        Document dokument = new Document(PageSize.A4.rotate());
        try (OutputStream out = Files.newOutputStream(zielPfad)) {
            PdfWriter.getInstance(dokument, out);
            dokument.open();

            // Titel
            Font titelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            dokument.add(new Paragraph("Bestandsbericht – " + LocalDate.now(), titelFont));
            dokument.add(new Paragraph(" "));

            // Tabelle mit 7 Spalten
            PdfPTable tabelle = new PdfPTable(7);
            tabelle.setWidthPercentage(100);
            tabelle.setWidths(new float[]{2f, 3f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});

            String[] spalten = {"Artikelnummer", "Bezeichnung", "Einheit", "Bestand", "Bestellpunkt", "Sicherheitsbestand", "Status"};
            Font kopfFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            for (String spalte : spalten) {
                PdfPCell zelle = new PdfPCell(new Phrase(spalte, kopfFont));
                zelle.setBackgroundColor(new Color(60, 60, 60));
                zelle.setPadding(4);
                tabelle.addCell(zelle);
            }

            Font zeilenFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (var a : artikel) {
                boolean kritisch = a.getAktuellerBestand() <= a.getSicherheitsbestand();
                Color hintergrund = kritisch ? new Color(255, 220, 220) : Color.WHITE;

                addCell(tabelle, a.getArtikelnummer(), zeilenFont, hintergrund);
                addCell(tabelle, a.getBezeichnung(), zeilenFont, hintergrund);
                addCell(tabelle, a.getMengeneinheit(), zeilenFont, hintergrund);
                addCell(tabelle, String.valueOf(a.getAktuellerBestand()), zeilenFont, hintergrund);
                addCell(tabelle, String.valueOf(a.getBestellpunkt()), zeilenFont, hintergrund);
                addCell(tabelle, String.valueOf(a.getSicherheitsbestand()), zeilenFont, hintergrund);
                addCell(tabelle, kritisch ? "Kritisch" : "Normal", zeilenFont, hintergrund);
            }

            dokument.add(tabelle);
            log.info("Bestandsbericht erstellt: {} ({} Artikel)", dateiname, artikel.size());

        } catch (Exception e) {
            log.error("Fehler beim Erstellen des Bestandsberichts: {}", e.getMessage());
        } finally {
            if (dokument.isOpen()) {
                dokument.close();
            }
        }
    }

    private void addCell(PdfPTable tabelle, String text, Font font, Color hintergrund) {
        PdfPCell zelle = new PdfPCell(new Phrase(text != null ? text : "", font));
        zelle.setBackgroundColor(hintergrund);
        zelle.setPadding(3);
        tabelle.addCell(zelle);
    }
}
