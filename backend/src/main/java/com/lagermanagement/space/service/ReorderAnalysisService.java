package com.lagermanagement.space.service;

import com.lagermanagement.space.config.AppReorderProperties;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.domain.repository.TransaktionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReorderAnalysisService {

    private final ArtikelRepository artikelRepository;
    private final TransaktionRepository transaktionRepository;
    private final BestellvorschlagRepository bestellvorschlagRepository;
    private final AppReorderProperties reorderProperties;

    // In-Memory: Zeitpunkt des letzten Analyse-Laufs (für KPI-Dashboard)
    private volatile LocalDateTime letzterReorderLauf;

    /**
     * Führt die Reorder-Analyse durch und gibt die Anzahl neu erstellter Vorschläge zurück.
     */
    public int runAnalysis() {
        int consumptionDays = reorderProperties.getConsumptionPeriodDays();
        LocalDate vonDatum = LocalDate.now().minusDays(consumptionDays);
        LocalDate bisDatum = LocalDate.now();

        List<Artikel> kandidaten = artikelRepository
                .findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0);

        int neueVorschlaege = 0;

        for (Artikel artikel : kandidaten) {
            // Verbrauch der letzten N Tage berechnen (informativ)
            int verbrauchGesamt = transaktionRepository
                    .findByArtikelIdAndTypAndDatumBetween(artikel.getId(), TransaktionTyp.AUSGANG, vonDatum, bisDatum)
                    .stream().mapToInt(t -> t.getMenge()).sum();
            double tagesverbrauch = consumptionDays > 0 ? (double) verbrauchGesamt / consumptionDays : 0;

            // Bestandsprüfung: Bestellung nötig?
            if (artikel.getAktuellerBestand() > artikel.getBestellpunkt()) {
                continue;
            }

            // Duplikat-Prüfung: VORSCHLAG oder BESTELLT bereits aktiv → überspringen
            List<Bestellvorschlag> aktiveVorschlaege = bestellvorschlagRepository
                    .findByArtikelIdAndStatusIn(artikel.getId(),
                            List.of(VorschlagStatus.VORSCHLAG, VorschlagStatus.BESTELLT));
            if (!aktiveVorschlaege.isEmpty()) {
                log.debug("Duplikat-Vorschlag für Artikel {} übersprungen", artikel.getArtikelnummer());
                continue;
            }

            // IGNORIERT-Prüfung
            Optional<Bestellvorschlag> ignoriert = bestellvorschlagRepository
                    .findTopByArtikelIdAndStatusOrderByErstelltAmDesc(artikel.getId(), VorschlagStatus.IGNORIERT);
            if (ignoriert.isPresent()) {
                if (artikel.getAktuellerBestand() > artikel.getSicherheitsbestand()) {
                    log.debug("IGNORIERT-Vorschlag für Artikel {} – Bestand über Sicherheitsbestand, kein neuer Vorschlag",
                            artikel.getArtikelnummer());
                    continue;
                }
                // Kritisch: IGNORIERT, aber Bestand <= Sicherheitsbestand → neuer Vorschlag erzwingen
                log.warn("Kritischer Bestand trotz IGNORIERT für Artikel {} (Bestand={}, Sicherheitsbestand={})",
                        artikel.getArtikelnummer(), artikel.getAktuellerBestand(), artikel.getSicherheitsbestand());
            }

            bestellvorschlagRepository.save(Bestellvorschlag.builder()
                    .artikel(artikel)
                    .lieferant(artikel.getLieferant())
                    .vorgeschlageneMenge(artikel.getStandardBestellmenge())
                    .bestandBeiErstellung(artikel.getAktuellerBestand())
                    .status(VorschlagStatus.VORSCHLAG)
                    .build());

            neueVorschlaege++;
            log.info("Neuer Bestellvorschlag für Artikel {} (Bestand={}, Bestellpunkt={}, Tagesverbrauch={:.2f})",
                    artikel.getArtikelnummer(), artikel.getAktuellerBestand(),
                    artikel.getBestellpunkt(), tagesverbrauch);
        }

        letzterReorderLauf = LocalDateTime.now();
        log.info("Reorder-Analyse abgeschlossen – neue Vorschläge: {}", neueVorschlaege);
        return neueVorschlaege;
    }

    public LocalDateTime getLetzterReorderLauf() {
        return letzterReorderLauf;
    }
}
