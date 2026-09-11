package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.BestellungRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.BestellungResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private BestellungRepository bestellungRepository;
    @Mock private BestellvorschlagRepository bestellvorschlagRepository;
    @Mock private PdfGeneratorService pdfGeneratorService;
    @Mock private XmlGeneratorService xmlGeneratorService;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    private OrderService orderService;

    private Lieferant testLieferant;
    private Artikel testArtikel;
    private Bestellvorschlag testVorschlag;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                bestellungRepository,
                bestellvorschlagRepository,
                pdfGeneratorService,
                xmlGeneratorService
        );

        // Security-Context simulieren
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("operator");
        SecurityContextHolder.setContext(securityContext);

        testLieferant = Lieferant.builder()
                .id(1L)
                .lieferantId("LF-001")
                .name("Testlieferant GmbH")
                .leadTimeTage(5)
                .build();

        testArtikel = Artikel.builder()
                .id(1L)
                .artikelnummer("ART-001")
                .bezeichnung("Testartikel")
                .mengeneinheit("Stück")
                .standardBestellmenge(50)
                .einkaufspreis(new BigDecimal("12.50"))
                .lieferant(testLieferant)
                .build();

        testVorschlag = Bestellvorschlag.builder()
                .id(1L)
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestandBeiErstellung(3)
                .vorgeschlageneMenge(50)
                .status(VorschlagStatus.VORSCHLAG)
                .erstelltAm(LocalDateTime.now())
                .aktualisiertAm(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldErstelleSchnellbestellungWithStandardBestellmenge() {
        when(bestellvorschlagRepository.findById(1L)).thenReturn(Optional.of(testVorschlag));
        when(bestellungRepository.countByErstelltAmBetween(any(), any())).thenReturn(0L);

        Bestellung gespeichert = Bestellung.builder()
                .id(10L)
                .bestellnummer("BEST-2026-001")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(50)
                .einkaufspreis(new BigDecimal("12.50"))
                .erstelltVon("operator")
                .status(BestellungStatus.OFFEN)
                .erstelltAm(LocalDateTime.now())
                .aktualisiertAm(LocalDateTime.now())
                .build();
        when(bestellungRepository.save(any())).thenReturn(gespeichert);
        when(bestellvorschlagRepository.save(any())).thenReturn(testVorschlag);
        when(pdfGeneratorService.generate(any())).thenReturn(Path.of("Bestellung_BEST-2026-001_2026-01-01.pdf"));
        when(xmlGeneratorService.generate(any())).thenReturn(Path.of("Bestellung_BEST-2026-001_2026-01-01.xml"));

        BestellungResponse response = orderService.erstelleSchnellbestellung(1L);

        assertThat(response.bestellungId()).isEqualTo(10L);
        assertThat(response.bestellnummer()).isEqualTo("BEST-2026-001");
        assertThat(response.pdfPfad()).endsWith(".pdf");
        assertThat(response.xmlPfad()).endsWith(".xml");

        // Bestellmenge entspricht StandardBestellmenge des Artikels
        verify(bestellungRepository).save(argThat(b -> b.getBestellmenge() == 50));
        // Vorschlag-Status wurde auf BESTELLT gesetzt
        verify(bestellvorschlagRepository).save(argThat(v -> v.getStatus() == VorschlagStatus.BESTELLT));
    }

    @Test
    void shouldSetAuditTrailErstelltVonOnBestellung() {
        when(bestellvorschlagRepository.findById(1L)).thenReturn(Optional.of(testVorschlag));
        when(bestellungRepository.countByErstelltAmBetween(any(), any())).thenReturn(2L);

        Bestellung gespeichert = Bestellung.builder()
                .id(3L).bestellnummer("BEST-2026-003")
                .artikel(testArtikel).lieferant(testLieferant)
                .bestellmenge(50).einkaufspreis(new BigDecimal("12.50"))
                .erstelltVon("operator").status(BestellungStatus.OFFEN)
                .erstelltAm(LocalDateTime.now()).aktualisiertAm(LocalDateTime.now()).build();
        when(bestellungRepository.save(any())).thenReturn(gespeichert);
        when(bestellvorschlagRepository.save(any())).thenReturn(testVorschlag);
        when(pdfGeneratorService.generate(any())).thenReturn(Path.of("x.pdf"));
        when(xmlGeneratorService.generate(any())).thenReturn(Path.of("x.xml"));

        orderService.erstelleSchnellbestellung(1L);

        verify(bestellungRepository).save(argThat(b -> "operator".equals(b.getErstelltVon())));
    }

    @Test
    void shouldIgnoriereVorschlag() {
        when(bestellvorschlagRepository.findById(1L)).thenReturn(Optional.of(testVorschlag));
        when(bestellvorschlagRepository.save(any())).thenReturn(testVorschlag);

        orderService.ignoriereVorschlag(1L);

        verify(bestellvorschlagRepository).save(argThat(v -> v.getStatus() == VorschlagStatus.IGNORIERT));
    }

    @Test
    void shouldThrowEntityNotFoundWhenVorschlagNotExists() {
        when(bestellvorschlagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.erstelleSchnellbestellung(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldFindAllBestellungenOrderedByErstelltAm() {
        when(bestellungRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of());

        List<?> result = orderService.findAll();

        assertThat(result).isEmpty();
    }
}
