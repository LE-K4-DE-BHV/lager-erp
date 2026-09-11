package com.lagermanagement.space.service.batch;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.ImportLog;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.ImportLogRepository;
import com.lagermanagement.space.domain.repository.LieferantRepository;
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
class StammdatenImportServiceTest {

    @Mock private FileService fileService;
    @Mock private FileParser fileParser;
    @Mock private LieferantRepository lieferantRepository;
    @Mock private ArtikelRepository artikelRepository;
    @Mock private ImportLogRepository importLogRepository;

    @InjectMocks
    private StammdatenImportService stammdatenImportService;

    private Path lieferantenFile;
    private Path artikelFile;

    @BeforeEach
    void setUp() {
        lieferantenFile = Path.of("lieferanten_test.csv");
        artikelFile = Path.of("artikel_test.csv");
    }

    // --- Lieferanten-Import ---

    @Test
    void shouldImportLieferantSuccessfullyAndCreateSuccessLog() throws ParseException {
        String hash = "abc123";
        when(fileService.computeHash(lieferantenFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(lieferantenFile)).thenReturn(List.of(
                Map.of("lieferant_id", "L001", "name", "Muster GmbH", "lead_time_tage", "5")
        ));
        when(lieferantRepository.findByLieferantId("L001")).thenReturn(Optional.empty());
        when(lieferantRepository.save(any())).thenReturn(Lieferant.builder().build());

        stammdatenImportService.processLieferantenFile(lieferantenFile);

        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("SUCCESS");
        assertThat(logCaptor.getValue().getZeilenErfolgreich()).isEqualTo(1);
        assertThat(logCaptor.getValue().getZeilenFehlerhaft()).isEqualTo(0);
    }

    @Test
    void shouldSkipLieferantenFileWhenHashAlreadyExists() throws ParseException {
        String hash = "duplicate-hash";
        when(fileService.computeHash(lieferantenFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(true);

        stammdatenImportService.processLieferantenFile(lieferantenFile);

        verify(fileParser, never()).parse(any());
        verify(lieferantRepository, never()).save(any());
        verify(importLogRepository, never()).save(any());
    }

    @Test
    void shouldCreateWarningLogWhenSomeLieferantenRowsFail() throws ParseException {
        String hash = "partial-hash";
        when(fileService.computeHash(lieferantenFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(lieferantenFile)).thenReturn(List.of(
                Map.of("lieferant_id", "L001", "name", "Muster GmbH", "lead_time_tage", "5"),
                Map.of("lieferant_id", "", "name", "", "lead_time_tage", "3")  // Pflichtfeld leer
        ));
        when(lieferantRepository.findByLieferantId("L001")).thenReturn(Optional.empty());
        when(lieferantRepository.save(any())).thenReturn(Lieferant.builder().build());

        stammdatenImportService.processLieferantenFile(lieferantenFile);

        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("WARNING");
        assertThat(logCaptor.getValue().getZeilenErfolgreich()).isEqualTo(1);
        assertThat(logCaptor.getValue().getZeilenFehlerhaft()).isEqualTo(1);
    }

    @Test
    void shouldCreateErrorLogWhenLieferantenParseExceptionOccurs() throws ParseException {
        String hash = "error-hash";
        when(fileService.computeHash(lieferantenFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(lieferantenFile)).thenThrow(new ParseException("Pflicht-Spalten fehlen"));

        stammdatenImportService.processLieferantenFile(lieferantenFile);

        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("ERROR");
        verify(lieferantRepository, never()).save(any());
    }

    // --- Artikel-Import ---

    @Test
    void shouldImportArtikelSuccessfullyAndCreateSuccessLog() throws ParseException {
        String hash = "artikel-hash";
        Lieferant lieferant = Lieferant.builder().lieferantId("L001").name("Test GmbH").build();

        when(fileService.computeHash(artikelFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(artikelFile)).thenReturn(List.of(
                Map.of("artikelnummer", "ART-001", "bezeichnung", "Testware",
                        "mengeneinheit", "Stk", "warengruppe", "A", "lieferant_id", "L001")
        ));
        when(lieferantRepository.findByLieferantId("L001")).thenReturn(Optional.of(lieferant));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.empty());
        when(artikelRepository.save(any())).thenReturn(Artikel.builder().build());

        stammdatenImportService.processArtikelFile(artikelFile);

        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("SUCCESS");
        assertThat(logCaptor.getValue().getZeilenErfolgreich()).isEqualTo(1);
    }

    @Test
    void shouldSkipArtikelRowWhenLieferantNotFoundInDb() throws ParseException {
        String hash = "artikel-no-lieferant";
        when(fileService.computeHash(artikelFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(artikelFile)).thenReturn(List.of(
                Map.of("artikelnummer", "ART-002", "bezeichnung", "Testware",
                        "mengeneinheit", "Stk", "warengruppe", "B", "lieferant_id", "L999")
        ));
        when(lieferantRepository.findByLieferantId("L999")).thenReturn(Optional.empty());

        stammdatenImportService.processArtikelFile(artikelFile);

        verify(artikelRepository, never()).save(any());
        ArgumentCaptor<ImportLog> logCaptor = ArgumentCaptor.forClass(ImportLog.class);
        verify(importLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("ERROR");
        assertThat(logCaptor.getValue().getZeilenFehlerhaft()).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingArtikelOnUpsert() throws ParseException {
        String hash = "artikel-update-hash";
        Lieferant lieferant = Lieferant.builder().lieferantId("L001").name("Test GmbH").build();
        Artikel bestehend = Artikel.builder().artikelnummer("ART-001").bezeichnung("Alt").build();

        when(fileService.computeHash(artikelFile)).thenReturn(hash);
        when(importLogRepository.existsByDateiHash(hash)).thenReturn(false);
        when(fileParser.parse(artikelFile)).thenReturn(List.of(
                Map.of("artikelnummer", "ART-001", "bezeichnung", "Neu",
                        "mengeneinheit", "Stk", "warengruppe", "A", "lieferant_id", "L001")
        ));
        when(lieferantRepository.findByLieferantId("L001")).thenReturn(Optional.of(lieferant));
        when(artikelRepository.findByArtikelnummer("ART-001")).thenReturn(Optional.of(bestehend));
        when(artikelRepository.save(any())).thenReturn(bestehend);

        stammdatenImportService.processArtikelFile(artikelFile);

        ArgumentCaptor<Artikel> artikelCaptor = ArgumentCaptor.forClass(Artikel.class);
        verify(artikelRepository).save(artikelCaptor.capture());
        assertThat(artikelCaptor.getValue().getBezeichnung()).isEqualTo("Neu");
    }
}
