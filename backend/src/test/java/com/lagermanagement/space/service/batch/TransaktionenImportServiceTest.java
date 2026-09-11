package com.lagermanagement.space.service.batch;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.entity.ImportLog;
import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.*;
import com.lagermanagement.space.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaktionenImportServiceTest {

    @Mock private FileService fileService;
    @Mock private FileParser fileParser;
    @Mock private ArtikelRepository artikelRepository;
    @Mock private TransaktionRepository transaktionRepository;
    @Mock private BestellungRepository bestellungRepository;
    @Mock private BestellvorschlagRepository bestellvorschlagRepository;
    @Mock private ImportLogRepository importLogRepository;

    @InjectMocks
    private TransaktionenImportService transaktionenImportService;

    private Path eingangFile;
    private Path ausgangFile;
    private Artikel artikel;

    @BeforeEach
    void setUp() {
        eingangFile = Path.of("eingang_test.csv");
        ausgangFile = Path.of("ausgang_test.csv");
        artikel = Artikel.builder()
                .artikelnummer("ART-001")
                .aktuellerBestand(100)
                .build();
    }

    // --- Eingang ---

    @Test
    void shouldIncreaseBestandAndSaveTransaktionOnEingang() throws ParseException {
        String hash = "eingang-hash";
        when(fileService.computeHash(eingangFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(eingangFile)).thenReturn(List.of(
                Map.of("datum", "2026-04-01", "artikelnummer", "ART-001", "menge", "20")
        ));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.of(artikel));

        transaktionenImportService.processEingangFile(eingangFile);

        ArgumentCaptor<Artikel> artikelCaptor = ArgumentCaptor.forClass(Artikel.class);
        verify(artikelRepository).save(artikelCaptor.capture());
        assertThat(artikelCaptor.getValue().getAktuellerBestand()).isEqualTo(120);

        ArgumentCaptor<Transaktion> transCaptor = ArgumentCaptor.forClass(Transaktion.class);
        verify(transaktionRepository).save(transCaptor.capture());
        assertThat(transCaptor.getValue().getTyp()).isEqualTo(TransaktionTyp.EINGANG);
    }

    @Test
    void shouldSetBestellungGeliefertOnEingangWithBestellReferenz() throws ParseException {
        // R-L4: Eingang mit bestellung_referenz schließt die passende Bestellung
        String hash = "eingang-bestellung-hash";
        Bestellung bestellung = Bestellung.builder()
                .id(1L)
                .bestellnummer("BEST-2026-001")
                .status(BestellungStatus.OFFEN)
                .erstelltVon("operator")
                .build();
        Bestellvorschlag vorschlag = Bestellvorschlag.builder()
                .status(VorschlagStatus.BESTELLT)
                .bestandBeiErstellung(0)
                .vorgeschlageneMenge(10)
                .build();

        when(fileService.computeHash(eingangFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(eingangFile)).thenReturn(List.of(
                Map.of("datum", "2026-04-01", "artikelnummer", "ART-001",
                        "menge", "10", "bestellung_referenz", "BEST-2026-001")
        ));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.of(artikel));
        when(bestellungRepository.findByBestellnummer("BEST-2026-001")).thenReturn(Optional.of(bestellung));
        when(bestellvorschlagRepository.findByBestellungId(1L)).thenReturn(List.of(vorschlag));

        transaktionenImportService.processEingangFile(eingangFile);

        ArgumentCaptor<Bestellung> bestellungCaptor = ArgumentCaptor.forClass(Bestellung.class);
        verify(bestellungRepository).save(bestellungCaptor.capture());
        assertThat(bestellungCaptor.getValue().getStatus()).isEqualTo(BestellungStatus.GELIEFERT);

        ArgumentCaptor<Bestellvorschlag> vorschlagCaptor = ArgumentCaptor.forClass(Bestellvorschlag.class);
        verify(bestellvorschlagRepository).save(vorschlagCaptor.capture());
        assertThat(vorschlagCaptor.getValue().getStatus()).isEqualTo(VorschlagStatus.GELIEFERT);
    }

    @Test
    void shouldSkipEingangFileWhenHashAlreadyExists() throws ParseException {
        String hash = "duplicate-eingang";
        when(fileService.computeHash(eingangFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(true);

        transaktionenImportService.processEingangFile(eingangFile);

        verify(fileParser, never()).parse(any());
        verify(artikelRepository, never()).save(any());
    }

    // --- Ausgang ---

    @Test
    void shouldDecreaseBestandAndSaveTransaktionOnAusgang() throws ParseException {
        String hash = "ausgang-hash";
        when(fileService.computeHash(ausgangFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(ausgangFile)).thenReturn(List.of(
                Map.of("datum", "2026-04-02", "artikelnummer", "ART-001",
                        "menge", "30", "buchungstyp", "Verbrauch intern")
        ));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.of(artikel));

        transaktionenImportService.processAusgangFile(ausgangFile);

        ArgumentCaptor<Artikel> artikelCaptor = ArgumentCaptor.forClass(Artikel.class);
        verify(artikelRepository).save(artikelCaptor.capture());
        assertThat(artikelCaptor.getValue().getAktuellerBestand()).isEqualTo(70);

        ArgumentCaptor<Transaktion> transCaptor = ArgumentCaptor.forClass(Transaktion.class);
        verify(transaktionRepository).save(transCaptor.capture());
        assertThat(transCaptor.getValue().getTyp()).isEqualTo(TransaktionTyp.AUSGANG);
        assertThat(transCaptor.getValue().getBuchungstyp()).isEqualTo("Verbrauch intern");
    }

    @Test
    void shouldSkipAusgangRowWhenNegativbestandWouldOccur() throws ParseException {
        String hash = "ausgang-negativ-hash";
        artikel = Artikel.builder().artikelnummer("ART-001").aktuellerBestand(5).build();

        when(fileService.computeHash(ausgangFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(ausgangFile)).thenReturn(List.of(
                Map.of("datum", "2026-04-02", "artikelnummer", "ART-001",
                        "menge", "10", "buchungstyp", "Verlust")
        ));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.of(artikel));

        transaktionenImportService.processAusgangFile(ausgangFile);

        verify(artikelRepository, never()).save(any());
        verify(transaktionRepository, never()).save(any());

        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getZeilenFehlerhaft()).isEqualTo(1);
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("ERROR");
    }
}
