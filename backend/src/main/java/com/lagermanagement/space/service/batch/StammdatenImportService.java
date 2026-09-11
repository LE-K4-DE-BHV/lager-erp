package com.lagermanagement.space.service.batch;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.ImportLog;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import com.lagermanagement.space.domain.repository.ArtikelRepository;
import com.lagermanagement.space.domain.repository.ImportLogRepository;
import com.lagermanagement.space.domain.repository.LieferantRepository;
import com.lagermanagement.space.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verarbeitet Stammdaten-Import-Dateien (Lieferanten + Artikel) aus Input/Stammdaten/.
 * Lieferanten werden immer zuerst verarbeitet (FK-Abhängigkeit).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StammdatenImportService {

    private static final String INPUT_DIR = "Input/Stammdaten";
    private static final String PROCESSED_SUCCESS = "Processed/Success";
    private static final String PROCESSED_WARNING = "Processed/Warning";
    private static final String PROCESSED_ERROR = "Processed/Error";

    private final FileService fileService;
    private final FileParser fileParser;
    private final LieferantRepository lieferantRepository;
    private final ArtikelRepository artikelRepository;
    private final ImportLogRepository importLogRepository;

    public void processAll() {
        importStammdaten();
    }

    void importStammdaten() {
        Path inputDir = fileService.resolveOutputPath(INPUT_DIR);
        List<Path> allFiles = fileService.scanDirectory(inputDir, "*.{csv,xlsx}");

        if (allFiles.isEmpty()) {
            return;
        }

        // Lieferanten zuerst verarbeiten (FK-Abhängigkeit)
        List<Path> lieferantenFiles = allFiles.stream()
                .filter(p -> p.getFileName().toString().toLowerCase().startsWith("lieferanten_"))
                .toList();
        List<Path> artikelFiles = allFiles.stream()
                .filter(p -> p.getFileName().toString().toLowerCase().startsWith("artikel_"))
                .toList();

        lieferantenFiles.forEach(this::processLieferantenFile);
        artikelFiles.forEach(this::processArtikelFile);
    }

    @Transactional
    public void processLieferantenFile(Path file) {
        String dateiname = file.getFileName().toString();
        String hash = fileService.computeHash(file);

        if (importLogRepository.existsByDateiHash(hash)) {
            log.info("Datei bereits verarbeitet, wird übersprungen: {}", dateiname);
            return;
        }

        List<Map<String, String>> zeilen;
        try {
            zeilen = fileParser.parse(file);
        } catch (ParseException e) {
            log.error("Datei konnte nicht geparst werden: {} – {}", dateiname, e.getMessage());
            speichereImportLog(dateiname, hash, "LIEFERANTEN", "ERROR", 0, 0, 0, e.getMessage());
            verschiebeInOrdner(file, PROCESSED_ERROR);
            return;
        }

        int gesamt = zeilen.size();
        int erfolgreich = 0;
        int fehlerhaft = 0;
        List<String> fehlerDetails = new ArrayList<>();

        for (Map<String, String> zeile : zeilen) {
            String lieferantId = zeile.get("lieferant_id");
            String name = zeile.get("name");
            String leadTimeTageStr = zeile.get("lead_time_tage");

            if (isBlank(lieferantId) || isBlank(name)) {
                String fehler = "Pflichtfeld 'lieferant_id' oder 'name' leer in Zeile";
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            try {
                Optional<Lieferant> vorhandener = lieferantRepository.findByLieferantId(lieferantId);
                Lieferant lieferant = vorhandener.orElse(Lieferant.builder().build());
                lieferant.setLieferantId(lieferantId);
                lieferant.setName(name);
                if (!isBlank(leadTimeTageStr)) {
                    lieferant.setLeadTimeTage(Integer.parseInt(leadTimeTageStr.trim()));
                }
                lieferantRepository.save(lieferant);
                erfolgreich++;
            } catch (NumberFormatException e) {
                String fehler = "Ungültiger lead_time_tage-Wert für lieferant_id=" + lieferantId;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
            }
        }

        String status = bestimmeStatus(gesamt, fehlerhaft);
        speichereImportLog(dateiname, hash, "LIEFERANTEN", status, gesamt, erfolgreich, fehlerhaft,
                fehlerDetails.isEmpty() ? null : String.join("; ", fehlerDetails));
        verschiebeInOrdner(file, statusZuOrdner(status));
        log.info("Lieferanten-Import abgeschlossen: {} – Status={}, OK={}, Fehler={}", dateiname, status, erfolgreich, fehlerhaft);
    }

    @Transactional
    public void processArtikelFile(Path file) {
        String dateiname = file.getFileName().toString();
        String hash = fileService.computeHash(file);

        if (importLogRepository.existsByDateiHash(hash)) {
            log.info("Datei bereits verarbeitet, wird übersprungen: {}", dateiname);
            return;
        }

        List<Map<String, String>> zeilen;
        try {
            zeilen = fileParser.parse(file);
        } catch (ParseException e) {
            log.error("Datei konnte nicht geparst werden: {} – {}", dateiname, e.getMessage());
            speichereImportLog(dateiname, hash, "ARTIKEL", "ERROR", 0, 0, 0, e.getMessage());
            verschiebeInOrdner(file, PROCESSED_ERROR);
            return;
        }

        int gesamt = zeilen.size();
        int erfolgreich = 0;
        int fehlerhaft = 0;
        List<String> fehlerDetails = new ArrayList<>();

        for (Map<String, String> zeile : zeilen) {
            String artikelnummer = zeile.get("artikelnummer");
            String bezeichnung = zeile.get("bezeichnung");
            String mengeneinheit = zeile.get("mengeneinheit");
            String warengruppe = zeile.get("warengruppe");
            String lieferantId = zeile.get("lieferant_id");

            if (isBlank(artikelnummer) || isBlank(bezeichnung) || isBlank(mengeneinheit) || isBlank(warengruppe)) {
                String fehler = "Pflichtfeld leer in Zeile für artikelnummer=" + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            if (isBlank(lieferantId)) {
                String fehler = "lieferant_id fehlt für artikelnummer=" + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            Optional<Lieferant> lieferantOpt = lieferantRepository.findByLieferantId(lieferantId);
            if (lieferantOpt.isEmpty()) {
                String fehler = "Lieferant nicht gefunden: lieferant_id=" + lieferantId + " für artikelnummer=" + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            try {
                Optional<Artikel> vorhandener = artikelRepository.findByArtikelnummer(artikelnummer);
                Artikel artikel = vorhandener.orElse(Artikel.builder().build());
                artikel.setArtikelnummer(artikelnummer);
                artikel.setBezeichnung(bezeichnung);
                artikel.setMengeneinheit(mengeneinheit);
                artikel.setWarengruppe(warengruppe);
                artikel.setLieferant(lieferantOpt.get());
                if (artikel.getStatus() == null) {
                    artikel.setStatus(ArtikelStatus.AKTIV);
                }
                artikelRepository.save(artikel);
                erfolgreich++;
            } catch (Exception e) {
                String fehler = "Datenbankfehler für artikelnummer=" + artikelnummer + ": " + e.getMessage();
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
            }
        }

        String status = bestimmeStatus(gesamt, fehlerhaft);
        speichereImportLog(dateiname, hash, "ARTIKEL", status, gesamt, erfolgreich, fehlerhaft,
                fehlerDetails.isEmpty() ? null : String.join("; ", fehlerDetails));
        verschiebeInOrdner(file, statusZuOrdner(status));
        log.info("Artikel-Import abgeschlossen: {} – Status={}, OK={}, Fehler={}", dateiname, status, erfolgreich, fehlerhaft);
    }

    private String bestimmeStatus(int gesamt, int fehlerhaft) {
        if (fehlerhaft == 0) return "SUCCESS";
        if (fehlerhaft >= gesamt) return "ERROR";
        return "WARNING";
    }

    private String statusZuOrdner(String status) {
        return switch (status) {
            case "SUCCESS" -> PROCESSED_SUCCESS;
            case "WARNING" -> PROCESSED_WARNING;
            default -> PROCESSED_ERROR;
        };
    }

    private void speichereImportLog(String dateiname, String hash, String typ, String status,
                                    int gesamt, int erfolgreich, int fehlerhaft, String fehlerDetails) {
        importLogRepository.save(ImportLog.builder()
                .dateiname(dateiname)
                .dateiHash(hash)
                .typ(typ)
                .status(status)
                .zeilenGesamt(gesamt)
                .zeilenErfolgreich(erfolgreich)
                .zeilenFehlerhaft(fehlerhaft)
                .fehlerDetails(fehlerDetails)
                .build());
    }

    private void verschiebeInOrdner(Path file, String zielSubPath) {
        try {
            fileService.moveFile(file, fileService.resolveOutputPath(zielSubPath));
        } catch (Exception e) {
            log.error("Datei konnte nicht verschoben werden: {} – {}", file.getFileName(), e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
