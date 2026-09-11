package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import com.lagermanagement.space.exception.BusinessException;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.AusgangRequest;
import com.lagermanagement.space.web.dto.BewegungResponse;
import com.lagermanagement.space.web.dto.EingangRequest;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.BestellungRepository;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.domain.repository.TransaktionRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryServiceTest {

    @Mock private ArtikelRepository artikelRepository;
    @Mock private TransaktionRepository transaktionRepository;
    @Mock private BestellungRepository bestellungRepository;
    @Mock private BestellvorschlagRepository bestellvorschlagRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    private InventoryService inventoryService;

    private Artikel testArtikel;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                artikelRepository, transaktionRepository, bestellungRepository, bestellvorschlagRepository);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("operator");
        SecurityContextHolder.setContext(securityContext);

        testArtikel = Artikel.builder()
                .id(1L)
                .artikelnummer("ART-001")
                .bezeichnung("Testartikel")
                .mengeneinheit("Stück")
                .aktuellerBestand(20)
                .build();
    }

    @Test
    void shouldBucheEingangAndIncreaseBestand() {
        EingangRequest request = new EingangRequest(1L, 10, LocalDate.now());
        Bestellung offeneBestellung = Bestellung.builder()
                .id(1L).bestellnummer("BEST-2026-001")
                .status(BestellungStatus.OFFEN)
                .erstelltVon("operator")
                .erstelltAm(LocalDateTime.now())
                .aktualisiertAm(LocalDateTime.now())
                .build();

        when(artikelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testArtikel));
        when(bestellungRepository.findAllByArtikelIdAndStatus(1L, BestellungStatus.OFFEN))
                .thenReturn(List.of(offeneBestellung));
        when(artikelRepository.save(any())).thenReturn(testArtikel);

        Transaktion gespeicherteTranksation = Transaktion.builder()
                .id(10L).typ(TransaktionTyp.EINGANG).menge(10).datum(LocalDate.now()).build();
        when(transaktionRepository.save(any())).thenReturn(gespeicherteTranksation);

        BewegungResponse response = inventoryService.bucheEingang(request);

        assertThat(response.transaktionId()).isEqualTo(10L);
        assertThat(response.bestellungKannGeschlossenWerden()).isTrue();
        // Bestand wurde erhöht
        verify(artikelRepository).save(argThat(a -> a.getAktuellerBestand() == 30));
    }

    @Test
    void shouldThrowBusinessExceptionWhenNoOffeneBestellungOnEingang() {
        EingangRequest request = new EingangRequest(1L, 5, LocalDate.now());

        when(artikelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testArtikel));
        when(bestellungRepository.findAllByArtikelIdAndStatus(1L, BestellungStatus.OFFEN))
                .thenReturn(List.of());

        assertThatThrownBy(() -> inventoryService.bucheEingang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Keine offene Bestellung");
    }

    @Test
    void shouldThrowEntityNotFoundWhenArtikelNotExistsOnEingang() {
        EingangRequest request = new EingangRequest(99L, 5, LocalDate.now());
        when(artikelRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.bucheEingang(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldBucheAusgangAndDecreaseBestand() {
        AusgangRequest request = new AusgangRequest(1L, 5, LocalDate.now(), "Verbrauch intern", "Testbedarf");

        when(artikelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testArtikel));
        when(artikelRepository.save(any())).thenReturn(testArtikel);
        when(transaktionRepository.save(any())).thenReturn(Transaktion.builder().id(5L).build());

        inventoryService.bucheAusgang(request);

        verify(artikelRepository).save(argThat(a -> a.getAktuellerBestand() == 15));
    }

    @Test
    void shouldThrowBusinessExceptionWhenNegativeBestandOnAusgang() {
        AusgangRequest request = new AusgangRequest(1L, 25, LocalDate.now(), "Verbrauch intern", null);
        // testArtikel hat Bestand 20, Ausgang 25 → Negativbestand

        when(artikelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testArtikel));

        assertThatThrownBy(() -> inventoryService.bucheAusgang(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nicht genügend Bestand");
    }

    @Test
    void shouldSetBenutzerId_OnEingang() {
        EingangRequest request = new EingangRequest(1L, 10, LocalDate.now());
        Bestellung offeneBestellung = Bestellung.builder()
                .id(1L).bestellnummer("BEST-2026-001")
                .status(BestellungStatus.OFFEN)
                .erstelltVon("operator")
                .erstelltAm(LocalDateTime.now()).aktualisiertAm(LocalDateTime.now()).build();

        when(artikelRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testArtikel));
        when(bestellungRepository.findAllByArtikelIdAndStatus(1L, BestellungStatus.OFFEN))
                .thenReturn(List.of(offeneBestellung));
        when(artikelRepository.save(any())).thenReturn(testArtikel);
        when(transaktionRepository.save(any())).thenReturn(
                Transaktion.builder().id(1L).typ(TransaktionTyp.EINGANG).menge(10).datum(LocalDate.now()).build());

        inventoryService.bucheEingang(request);

        // Audit-Trail: benutzerId muss gesetzt sein
        verify(transaktionRepository).save(argThat(t -> "operator".equals(t.getBenutzerId())));
    }
}
