package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.repository.TransaktionRepository;
import com.lagermanagement.space.web.dto.TransaktionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transaktionen")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransaktionController {

    private final TransaktionRepository transaktionRepository;

    @GetMapping
    public Page<TransaktionDto> findAll(
            @RequestParam(required = false) Long artikelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(defaultValue = "0") int seite,
            @RequestParam(defaultValue = "50") int groesse
    ) {
        Pageable pageable = PageRequest.of(seite, groesse, Sort.by(Sort.Direction.DESC, "datum", "erstelltAm"));

        LocalDate datumVon = von != null ? von : LocalDate.MIN;
        LocalDate datumBis = bis != null ? bis : LocalDate.now();

        Page<Transaktion> seite1;

        if (artikelId != null && (von != null || bis != null)) {
            seite1 = transaktionRepository.findAllByArtikelIdAndDatumBetween(artikelId, datumVon, datumBis, pageable);
        } else if (artikelId != null) {
            seite1 = transaktionRepository.findAllByArtikelId(artikelId, pageable);
        } else if (von != null || bis != null) {
            seite1 = transaktionRepository.findAllByDatumBetween(datumVon, datumBis, pageable);
        } else {
            seite1 = transaktionRepository.findAll(pageable);
        }

        return seite1.map(this::toDto);
    }

    private TransaktionDto toDto(Transaktion t) {
        return new TransaktionDto(
                t.getId(),
                t.getArtikel() != null ? t.getArtikel().getArtikelnummer() : null,
                t.getArtikel() != null ? t.getArtikel().getBezeichnung() : null,
                t.getTyp(),
                t.getBuchungstyp(),
                t.getMenge(),
                t.getDatum(),
                t.getQuelle(),
                t.getBenutzerId(),
                t.getBestellung() != null ? t.getBestellung().getBestellnummer() : null
        );
    }
}
