package com.lagermanagement.space.service;

import com.lagermanagement.space.config.AppReorderProperties;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.domain.repository.TransaktionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReorderAnalysisServiceTest {

    @Mock private ArtikelRepository artikelRepository;
    @Mock private TransaktionRepository transaktionRepository;
    @Mock private BestellvorschlagRepository bestellvorschlagRepository;

    private ReorderAnalysisService service;

    @BeforeEach
    void setUp() {
        AppReorderProperties props = new AppReorderProperties();
        props.setConsumptionPeriodDays(30);
        service = new ReorderAnalysisService(artikelRepository, transaktionRepository, bestellvorschlagRepository, props);
        when(transaktionRepository.findByArtikelIdAndTypAndDatumBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
    }

    private Artikel artikelMit(int bestand, int bestellpunkt, int sicherheitsbestand, int standardBestellmenge) {
        Artikel a = new Artikel();
        a.setId(1L);
        a.setArtikelnummer("ART-001");
        a.setBezeichnung("Test");
        a.setMengeneinheit("Stk");
        a.setWarengruppe("A");
        a.setAktuellerBestand(bestand);
        a.setBestellpunkt(bestellpunkt);
        a.setSicherheitsbestand(sicherheitsbestand);
        a.setStandardBestellmenge(standardBestellmenge);
        a.setStatus(ArtikelStatus.AKTIV);
        return a;
    }

    @Test
    void shouldCreateVorschlagWhenBestandUnterBestellpunkt() {
        Artikel artikel = artikelMit(5, 10, 3, 20);
        when(artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0))
                .thenReturn(List.of(artikel));
        when(bestellvorschlagRepository.findByArtikelIdAndStatusIn(any(), any())).thenReturn(List.of());
        when(bestellvorschlagRepository.findTopByArtikelIdAndStatusOrderByErstelltAmDesc(any(), any()))
                .thenReturn(Optional.empty());

        int result = service.runAnalysis();

        assertThat(result).isEqualTo(1);
        verify(bestellvorschlagRepository).save(argThat(v -> v.getVorgeschlageneMenge() == 20));
    }

    @Test
    void shouldNotCreateVorschlagWhenBestandUeberBestellpunkt() {
        Artikel artikel = artikelMit(15, 10, 3, 20);
        when(artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0))
                .thenReturn(List.of(artikel));

        int result = service.runAnalysis();

        assertThat(result).isEqualTo(0);
        verify(bestellvorschlagRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateDuplicateWhenVorschlagAktiv() {
        Artikel artikel = artikelMit(5, 10, 3, 20);
        when(artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0))
                .thenReturn(List.of(artikel));
        when(bestellvorschlagRepository.findByArtikelIdAndStatusIn(any(), any()))
                .thenReturn(List.of(new Bestellvorschlag()));

        int result = service.runAnalysis();

        assertThat(result).isEqualTo(0);
        verify(bestellvorschlagRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateVorschlagWhenIgnoriertUndBestandUeberSicherheitsbestand() {
        Artikel artikel = artikelMit(5, 10, 3, 20);
        when(artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0))
                .thenReturn(List.of(artikel));
        when(bestellvorschlagRepository.findByArtikelIdAndStatusIn(any(), any())).thenReturn(List.of());
        when(bestellvorschlagRepository.findTopByArtikelIdAndStatusOrderByErstelltAmDesc(any(), eq(VorschlagStatus.IGNORIERT)))
                .thenReturn(Optional.of(new Bestellvorschlag()));

        int result = service.runAnalysis();

        assertThat(result).isEqualTo(0);
        verify(bestellvorschlagRepository, never()).save(any());
    }

    @Test
    void shouldCreateVorschlagWhenIgnoriertAberKritischerBestand() {
        // Bestand (2) <= Sicherheitsbestand (3) → kritisch, trotz IGNORIERT neuer Vorschlag
        Artikel artikel = artikelMit(2, 10, 3, 20);
        when(artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0))
                .thenReturn(List.of(artikel));
        when(bestellvorschlagRepository.findByArtikelIdAndStatusIn(any(), any())).thenReturn(List.of());
        when(bestellvorschlagRepository.findTopByArtikelIdAndStatusOrderByErstelltAmDesc(any(), eq(VorschlagStatus.IGNORIERT)))
                .thenReturn(Optional.of(new Bestellvorschlag()));

        int result = service.runAnalysis();

        assertThat(result).isEqualTo(1);
        verify(bestellvorschlagRepository).save(any());
    }
}
