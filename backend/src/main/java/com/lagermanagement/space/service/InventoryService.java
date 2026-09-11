package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.TransaktionQuelle;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.BestellungRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.domain.repository.TransaktionRepository;
import com.lagermanagement.space.exception.BusinessException;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.AusgangRequest;
import com.lagermanagement.space.web.dto.BewegungResponse;
import com.lagermanagement.space.web.dto.EingangRequest;
import com.lagermanagement.space.web.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ArtikelRepository artikelRepository;
    private final TransaktionRepository transaktionRepository;
    private final BestellungRepository bestellungRepository;
    private final BestellvorschlagRepository bestellvorschlagRepository;

    /**
     * Bucht einen manuellen Wareneingang. Setzt einen pessimistischen Lock auf den Artikel,
     * um Race-Conditions bei parallelen Buchungen zu verhindern.
     * Eine offene Bestellung ist Voraussetzung für den Eingang.
     */
    @Transactional
    public BewegungResponse bucheEingang(EingangRequest request) {
        Artikel artikel = artikelRepository.findByIdWithLock(request.artikelId())
                .orElseThrow(() -> new EntityNotFoundException("Artikel mit ID " + request.artikelId() + " nicht gefunden."));

        List<Bestellung> offeneBestellungen = bestellungRepository
                .findAllByArtikelIdAndStatus(request.artikelId(), BestellungStatus.OFFEN);
        if (offeneBestellungen.isEmpty()) {
            throw new BusinessException("Keine offene Bestellung für diesen Artikel. Bitte zuerst eine Bestellung anlegen.");
        }

        artikel.setAktuellerBestand(artikel.getAktuellerBestand() + request.menge());
        artikelRepository.save(artikel);

        Transaktion transaktion = Transaktion.builder()
                .artikel(artikel)
                .typ(TransaktionTyp.EINGANG)
                .buchungstyp("WARENEINGANG")
                .menge(request.menge())
                .datum(request.datum())
                .quelle(TransaktionQuelle.MANUELL)
                .benutzerId(SecurityUtils.getCurrentUsername())
                .build();

        Transaktion gespeichert = transaktionRepository.save(transaktion);
        log.info("Wareneingang gebucht: Artikel={}, Menge={}, Benutzer={}",
                artikel.getArtikelnummer(), request.menge(), transaktion.getBenutzerId());

        return new BewegungResponse(gespeichert.getId(), true);
    }

    /**
     * Schließt die zugehörige Bestellung nach einem Wareneingang ab.
     * Setzt Bestellstatus auf GELIEFERT und aktualisiert den Bestellvorschlag.
     */
    @Transactional
    public void bestellungAbschliessen(Long transaktionId) {
        Transaktion transaktion = transaktionRepository.findById(transaktionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaktion mit ID " + transaktionId + " nicht gefunden."));

        List<Bestellung> offeneBestellungen = bestellungRepository
                .findAllByArtikelIdAndStatus(transaktion.getArtikel().getId(), BestellungStatus.OFFEN);

        if (!offeneBestellungen.isEmpty()) {
            Bestellung bestellung = offeneBestellungen.get(0);
            bestellung.setStatus(BestellungStatus.GELIEFERT);
            bestellungRepository.save(bestellung);

            // Zugehörige Bestellvorschläge ebenfalls auf GELIEFERT setzen
            bestellvorschlagRepository.findByBestellungId(bestellung.getId())
                    .forEach(vorschlag -> {
                        vorschlag.setStatus(VorschlagStatus.GELIEFERT);
                        bestellvorschlagRepository.save(vorschlag);
                    });

            log.info("Bestellung {} als geliefert markiert.", bestellung.getBestellnummer());
        }
    }

    /**
     * Bucht einen manuellen Warenausgang. Verhindert Negativbestand.
     * Setzt pessimistischen Lock auf den Artikel.
     */
    @Transactional
    public void bucheAusgang(AusgangRequest request) {
        Artikel artikel = artikelRepository.findByIdWithLock(request.artikelId())
                .orElseThrow(() -> new EntityNotFoundException("Artikel mit ID " + request.artikelId() + " nicht gefunden."));

        if (artikel.getAktuellerBestand() - request.menge() < 0) {
            throw new BusinessException(
                    "Nicht genügend Bestand. Verfügbar: " + artikel.getAktuellerBestand()
                            + ", angefordert: " + request.menge());
        }

        artikel.setAktuellerBestand(artikel.getAktuellerBestand() - request.menge());
        artikelRepository.save(artikel);

        Transaktion transaktion = Transaktion.builder()
                .artikel(artikel)
                .typ(TransaktionTyp.AUSGANG)
                .buchungstyp(request.buchungstyp())
                .menge(request.menge())
                .datum(request.datum())
                .quelle(TransaktionQuelle.MANUELL)
                .grund(request.grund())
                .benutzerId(SecurityUtils.getCurrentUsername())
                .build();

        transaktionRepository.save(transaktion);
        log.info("Warenausgang gebucht: Artikel={}, Menge={}, Buchungstyp={}, Benutzer={}",
                artikel.getArtikelnummer(), request.menge(), request.buchungstyp(), transaktion.getBenutzerId());
    }
}
