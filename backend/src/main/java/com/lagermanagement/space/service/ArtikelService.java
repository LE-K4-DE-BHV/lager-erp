package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.LieferantRepository;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.ArtikelCreateRequest;
import com.lagermanagement.space.web.dto.ArtikelDto;
import com.lagermanagement.space.web.dto.ArtikelUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtikelService {

    private final ArtikelRepository artikelRepository;
    private final LieferantRepository lieferantRepository;

    @Transactional(readOnly = true)
    public List<ArtikelDto> findAll() {
        return artikelRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtikelDto findById(Long id) {
        return toDto(ladeartikelOrThrow(id));
    }

    @Transactional
    public ArtikelDto create(ArtikelCreateRequest request) {
        if (artikelRepository.existsByArtikelnummer(request.artikelnummer())) {
            throw new DataIntegrityViolationException(
                    "Artikelnummer '" + request.artikelnummer() + "' existiert bereits.");
        }

        Lieferant lieferant = request.lieferantId() != null
                ? ladeLieferantOrThrow(request.lieferantId())
                : null;

        Artikel artikel = Artikel.builder()
                .artikelnummer(request.artikelnummer())
                .bezeichnung(request.bezeichnung())
                .mengeneinheit(request.mengeneinheit())
                .warengruppe(request.warengruppe())
                .lieferant(lieferant)
                .sicherheitsbestand(request.sicherheitsbestand())
                .bestellpunkt(request.bestellpunkt())
                .standardBestellmenge(request.standardBestellmenge())
                .einkaufspreis(request.einkaufspreis())
                .build();

        Artikel gespeichert = artikelRepository.save(artikel);
        log.info("Artikel angelegt: {} ({})", gespeichert.getArtikelnummer(), gespeichert.getId());
        return toDto(gespeichert);
    }

    @Transactional
    public ArtikelDto update(Long id, ArtikelUpdateRequest request) {
        Artikel artikel = ladeartikelOrThrow(id);

        Lieferant lieferant = request.lieferantId() != null
                ? ladeLieferantOrThrow(request.lieferantId())
                : null;

        // artikelnummer ist nach Anlage nicht editierbar
        artikel.setBezeichnung(request.bezeichnung());
        artikel.setMengeneinheit(request.mengeneinheit());
        artikel.setWarengruppe(request.warengruppe());
        artikel.setLieferant(lieferant);
        artikel.setSicherheitsbestand(request.sicherheitsbestand());
        artikel.setBestellpunkt(request.bestellpunkt());
        artikel.setStandardBestellmenge(request.standardBestellmenge());
        artikel.setEinkaufspreis(request.einkaufspreis());

        return toDto(artikelRepository.save(artikel));
    }

    @Transactional
    public ArtikelDto deaktivieren(Long id) {
        Artikel artikel = ladeartikelOrThrow(id);
        artikel.setStatus(ArtikelStatus.INAKTIV);
        log.info("Artikel deaktiviert: {}", id);
        return toDto(artikelRepository.save(artikel));
    }

    @Transactional(readOnly = true)
    public int getBestand(Long id) {
        return ladeartikelOrThrow(id).getAktuellerBestand();
    }

    // --- Hilfsmethoden ---

    private Artikel ladeartikelOrThrow(Long id) {
        return artikelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artikel mit ID " + id + " nicht gefunden."));
    }

    private Lieferant ladeLieferantOrThrow(Long id) {
        return lieferantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lieferant mit ID " + id + " nicht gefunden."));
    }

    ArtikelDto toDto(Artikel artikel) {
        return new ArtikelDto(
                artikel.getId(),
                artikel.getArtikelnummer(),
                artikel.getBezeichnung(),
                artikel.getMengeneinheit(),
                artikel.getWarengruppe(),
                artikel.getLieferant() != null ? artikel.getLieferant().getId() : null,
                artikel.getLieferant() != null ? artikel.getLieferant().getName() : null,
                artikel.getAktuellerBestand(),
                artikel.getSicherheitsbestand(),
                artikel.getBestellpunkt(),
                artikel.getStandardBestellmenge(),
                artikel.getEinkaufspreis(),
                artikel.getStatus()
        );
    }
}
