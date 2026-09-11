package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.BestellungRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.BestellungCreateRequest;
import com.lagermanagement.space.web.dto.BestellungDto;
import com.lagermanagement.space.web.dto.BestellungResponse;
import com.lagermanagement.space.web.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final BestellungRepository bestellungRepository;
    private final BestellvorschlagRepository bestellvorschlagRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final XmlGeneratorService xmlGeneratorService;

    @Transactional
    public BestellungResponse erstelleSchnellbestellung(Long vorschlagId) {
        Bestellvorschlag vorschlag = ladeVorschlagOrThrow(vorschlagId);

        BestellungCreateRequest request = new BestellungCreateRequest(
                vorschlag.getArtikel().getStandardBestellmenge(),
                vorschlag.getArtikel().getEinkaufspreis(),
                null,
                null
        );

        return erstelleBestellung(vorschlag, request);
    }

    @Transactional
    public BestellungResponse erstelleBearbeiteteBestellung(Long vorschlagId, BestellungCreateRequest request) {
        Bestellvorschlag vorschlag = ladeVorschlagOrThrow(vorschlagId);
        return erstelleBestellung(vorschlag, request);
    }

    @Transactional
    public void ignoriereVorschlag(Long vorschlagId) {
        Bestellvorschlag vorschlag = ladeVorschlagOrThrow(vorschlagId);
        vorschlag.setStatus(VorschlagStatus.IGNORIERT);
        bestellvorschlagRepository.save(vorschlag);
        log.info("Bestellvorschlag ignoriert: {}", vorschlagId);
    }

    @Transactional(readOnly = true)
    public List<BestellungDto> findAll() {
        return bestellungRepository.findAll(Sort.by(Sort.Direction.DESC, "erstelltAm")).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BestellungDto findById(Long id) {
        return toDto(ladeBestellungOrThrow(id));
    }

    // --- Interner Bestellungs-Ablauf ---

    private BestellungResponse erstelleBestellung(Bestellvorschlag vorschlag, BestellungCreateRequest request) {
        String bestellnummer = generiereBestellnummer();
        String benutzer = SecurityUtils.getCurrentUsername();

        Bestellung bestellung = Bestellung.builder()
                .bestellnummer(bestellnummer)
                .artikel(vorschlag.getArtikel())
                .lieferant(vorschlag.getLieferant())
                .bestellmenge(request.bestellmenge())
                .einkaufspreis(request.einkaufspreis())
                .gewuenschtesLieferdatum(request.gewuenschtesLieferdatum())
                .notiz(request.notiz())
                .erstelltVon(benutzer)
                .build();

        Bestellung gespeichert = bestellungRepository.save(bestellung);

        // Vorschlag-Status auf BESTELLT setzen und mit Bestellung verknüpfen
        vorschlag.setStatus(VorschlagStatus.BESTELLT);
        vorschlag.setBestellung(gespeichert);
        bestellvorschlagRepository.save(vorschlag);

        log.info("Bestellung erstellt: {} (Benutzer: {})", bestellnummer, benutzer);

        // Dokumente erzeugen – Fehler hier dürfen die Bestellung nicht rückgängig machen
        String pdfDateiname = null;
        String xmlDateiname = null;
        try {
            pdfDateiname = pdfGeneratorService.generate(gespeichert).getFileName().toString();
        } catch (Exception e) {
            log.error("PDF-Erstellung fehlgeschlagen für Bestellung {}: {}", bestellnummer, e.getMessage());
        }
        try {
            xmlDateiname = xmlGeneratorService.generate(gespeichert).getFileName().toString();
        } catch (Exception e) {
            log.error("XML-Erstellung fehlgeschlagen für Bestellung {}: {}", bestellnummer, e.getMessage());
        }

        return new BestellungResponse(
                gespeichert.getId(),
                bestellnummer,
                pdfDateiname,
                xmlDateiname
        );
    }

    private String generiereBestellnummer() {
        int jahr = LocalDateTime.now().getYear();
        LocalDateTime vonJahr = LocalDateTime.of(jahr, 1, 1, 0, 0);
        LocalDateTime bisJahr = LocalDateTime.of(jahr, 12, 31, 23, 59, 59);
        long anzahl = bestellungRepository.countByErstelltAmBetween(vonJahr, bisJahr);
        return String.format("BEST-%d-%03d", jahr, anzahl + 1);
    }

    // --- Hilfsmethoden ---

    private Bestellvorschlag ladeVorschlagOrThrow(Long id) {
        return bestellvorschlagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bestellvorschlag mit ID " + id + " nicht gefunden."));
    }

    private Bestellung ladeBestellungOrThrow(Long id) {
        return bestellungRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bestellung mit ID " + id + " nicht gefunden."));
    }

    BestellungDto toDto(Bestellung bestellung) {
        return new BestellungDto(
                bestellung.getId(),
                bestellung.getBestellnummer(),
                bestellung.getArtikel() != null ? bestellung.getArtikel().getId() : null,
                bestellung.getArtikel() != null ? bestellung.getArtikel().getArtikelnummer() : null,
                bestellung.getLieferant() != null ? bestellung.getLieferant().getName() : null,
                bestellung.getBestellmenge(),
                bestellung.getEinkaufspreis(),
                bestellung.getGewuenschtesLieferdatum(),
                bestellung.getNotiz(),
                bestellung.getStatus(),
                bestellung.getErstelltVon(),
                bestellung.getErstelltAm()
        );
    }
}
