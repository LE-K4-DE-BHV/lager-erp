package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.web.dto.xml.BestellungXml;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class XmlGeneratorService {

    private final FileService fileService;

    /**
     * Marshalt eine Bestellung als JAXB-XML und speichert sie in Output/Orders/.
     *
     * @return Pfad zur erzeugten XML-Datei
     */
    public Path generate(Bestellung bestellung) {
        String dateiname = "Bestellung_" + bestellung.getBestellnummer() + "_" + LocalDate.now() + ".xml";
        Path zielPfad = fileService.resolveOutputPath("Output/Orders").resolve(dateiname);

        try {
            Files.createDirectories(zielPfad.getParent());
        } catch (IOException e) {
            log.error("Ausgabeverzeichnis konnte nicht erstellt werden: {}", e.getMessage());
            throw new RuntimeException("XML-Ausgabeverzeichnis nicht erstellbar", e);
        }

        BigDecimal einkaufspreis = bestellung.getEinkaufspreis() != null
                ? bestellung.getEinkaufspreis()
                : BigDecimal.ZERO;
        BigDecimal gesamtbetrag = einkaufspreis.multiply(BigDecimal.valueOf(bestellung.getBestellmenge()));

        BestellungXml bestellungXml = new BestellungXml(
                bestellung.getBestellnummer(),
                bestellung.getLieferant() != null ? bestellung.getLieferant().getName() : null,
                bestellung.getArtikel() != null ? bestellung.getArtikel().getArtikelnummer() : null,
                bestellung.getArtikel() != null ? bestellung.getArtikel().getBezeichnung() : null,
                bestellung.getBestellmenge(),
                einkaufspreis.toPlainString(),
                gesamtbetrag.toPlainString(),
                bestellung.getGewuenschtesLieferdatum() != null
                        ? bestellung.getGewuenschtesLieferdatum().toString()
                        : null,
                bestellung.getNotiz(),
                bestellung.getErstelltVon(),
                bestellung.getErstelltAm() != null ? bestellung.getErstelltAm().toLocalDate().toString() : null
        );

        try {
            JAXBContext context = JAXBContext.newInstance(BestellungXml.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(bestellungXml, zielPfad.toFile());
            log.info("XML erstellt: {}", dateiname);
        } catch (JAXBException e) {
            log.error("Fehler beim Erstellen der Bestellungs-XML: {}", e.getMessage());
            throw new RuntimeException("XML-Erstellung fehlgeschlagen: " + e.getMessage(), e);
        }

        return zielPfad;
    }
}
