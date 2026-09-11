package com.lagermanagement.space.service.batch;

import com.lagermanagement.space.domain.entity.*;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import com.lagermanagement.space.domain.enums.TransaktionQuelle;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.*;
import com.lagermanagement.space.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verarbeitet Transaktions-Import-Dateien (Wareneingang + -ausgang) aus Input/Transaktionen/.
 * Bestandsaktualisierung und Transaktionsspeicherung laufen im gleichen @Transactional-Block.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransaktionenImportService {

    private static final String INPUT_DIR = "Input/Transaktionen";
    private static final String PROCESSED_SUCCESS = "Processed/Success";
    private static final String PROCESSED_WARNING = "Processed/Warning";
    private static final String PROCESSED_ERROR = "Processed/Error";

    private final FileService fileService;
    private final FileParser fileParser;
    private final ArtikelRepository artikelRepository;
    private final TransaktionRepository transaktionRepository;
    private final BestellungRepository bestellungRepository;
    private final BestellvorschlagRepository bestellvorschlagRepository;
    private final ImportLogRepository importLogRepository;

    public void processAll() {
        importTransaktionen();
    }

    void importTransaktionen() {
        Path inputDir = fileService.resolveOutputPath(INPUT_DIR);
        List<Path> allFiles = fileService.scanDirectory(inputDir, "*.{csv,xlsx}");

        if (allFiles.isEmpty()) {
            return;
        }

        List<Path> eingangFiles = allFiles.stream()
                .filter(p -> p.getFileName().toString().toLowerCase().startsWith("eingang_"))
                .toList();
        List<Path> ausgangFiles = allFiles.stream()
                .filter(p -> p.getFileName().toString().toLowerCase().startsWith("ausgang_"))
                .toList();

        eingangFiles.forEach(this::processEingangFile);
        ausgangFiles.forEach(this::processAusgangFile);
    }

    @Transactional
    public void processEingangFile(Path file) {
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
            speichereImportLog(dateiname, hash, "EINGANG", "ERROR", 0, 0, 0, e.getMessage());
            verschiebeInOrdner(file, PROCESSED_ERROR);
            return;
        }

        int gesamt = zeilen.size();
        int erfolgreich = 0;
        int fehlerhaft = 0;
        List<String> fehlerDetails = new ArrayList<>();

        for (Map<String, String> zeile : zeilen) {
            String datumStr = zeile.get("datum");
            String artikelnummer = zeile.get("artikelnummer");
            String mengeStr = zeile.get("menge");
            String bestellReferenz = zeile.get("bestellung_referenz");

            if (isBlank(datumStr) || isBlank(artikelnummer) || isBlank(mengeStr)) {
                String fehler = "Pflichtfeld fehlt in Eingang-Zeile";
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            LocalDate datum;
            int menge;
            try {
                datum = LocalDate.parse(datumStr.trim());
                menge = Integer.parseInt(mengeStr.trim());
            } catch (DateTimeParseException | NumberFormatException e) {
                String fehler = "Ungültiger Wert (datum oder menge) für artikelnummer=" + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            Optional<Artikel> artikelOpt = artikelRepository.findByArtikelnummer(artikelnummer);
            if (artikelOpt.isEmpty()) {
                String fehler = "Artikel nicht gefunden: " + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            Artikel artikel = artikelOpt.get();
            artikel.setAktuellerBestand(artikel.getAktuellerBestand() + menge);
            artikelRepository.save(artikel);

            // R-L4: Bestellung auf GELIEFERT setzen, wenn eine Bestellreferenz vorliegt
            if (!isBlank(bestellReferenz)) {
                bestellungRepository.findByBestellnummer(bestellReferenz.trim())
                        .filter(b -> b.getStatus() == BestellungStatus.OFFEN)
                        .ifPresent(bestellung -> {
                            bestellung.setStatus(BestellungStatus.GELIEFERT);
                            bestellungRepository.save(bestellung);
                            // Zugehörigen Bestellvorschlag ebenfalls auf GELIEFERT setzen
                            bestellvorschlagRepository.findByBestellungId(bestellung.getId())
                                    .forEach(vorschlag -> {
                                        vorschlag.setStatus(VorschlagStatus.GELIEFERT);
                                        bestellvorschlagRepository.save(vorschlag);
                                    });
                            log.info("Bestellung {} auf GELIEFERT gesetzt", bestellReferenz);
                        });
            }

            transaktionRepository.save(Transaktion.builder()
                    .artikel(artikel)
                    .typ(TransaktionTyp.EINGANG)
                    .quelle(TransaktionQuelle.BATCH)
                    .menge(menge)
                    .datum(datum)
                    .build());
            erfolgreich++;
        }

        String status = bestimmeStatus(gesamt, fehlerhaft);
        speichereImportLog(dateiname, hash, "EINGANG", status, gesamt, erfolgreich, fehlerhaft,
                fehlerDetails.isEmpty() ? null : String.join("; ", fehlerDetails));
        verschiebeInOrdner(file, statusZuOrdner(status));
        log.info("Eingang-Import abgeschlossen: {} – Status={}, OK={}, Fehler={}", dateiname, status, erfolgreich, fehlerhaft);
    }

    @Transactional
    public void processAusgangFile(Path file) {
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
            speichereImportLog(dateiname, hash, "AUSGANG", "ERROR", 0, 0, 0, e.getMessage());
            verschiebeInOrdner(file, PROCESSED_ERROR);
            return;
        }

        int gesamt = zeilen.size();
        int erfolgreich = 0;
        int fehlerhaft = 0;
        List<String> fehlerDetails = new ArrayList<>();

        for (Map<String, String> zeile : zeilen) {
            String datumStr = zeile.get("datum");
            String artikelnummer = zeile.get("artikelnummer");
            String mengeStr = zeile.get("menge");
            String buchungstyp = zeile.get("buchungstyp");

            if (isBlank(datumStr) || isBlank(artikelnummer) || isBlank(mengeStr) || isBlank(buchungstyp)) {
                String fehler = "Pflichtfeld fehlt in Ausgang-Zeile";
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            LocalDate datum;
            int menge;
            try {
                datum = LocalDate.parse(datumStr.trim());
                menge = Integer.parseInt(mengeStr.trim());
            } catch (DateTimeParseException | NumberFormatException e) {
                String fehler = "Ungültiger Wert (datum oder menge) für artikelnummer=" + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            Optional<Artikel> artikelOpt = artikelRepository.findByArtikelnummer(artikelnummer);
            if (artikelOpt.isEmpty()) {
                String fehler = "Artikel nicht gefunden: " + artikelnummer;
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            Artikel artikel = artikelOpt.get();
            if (artikel.getAktuellerBestand() - menge < 0) {
                String fehler = "Negativbestand verhindert für artikelnummer=" + artikelnummer
                        + " (Bestand=" + artikel.getAktuellerBestand() + ", Menge=" + menge + ")";
                log.warn("{}: {}", dateiname, fehler);
                fehlerDetails.add(fehler);
                fehlerhaft++;
                continue;
            }

            artikel.setAktuellerBestand(artikel.getAktuellerBestand() - menge);
            artikelRepository.save(artikel);

            transaktionRepository.save(Transaktion.builder()
                    .artikel(artikel)
                    .typ(TransaktionTyp.AUSGANG)
                    .quelle(TransaktionQuelle.BATCH)
                    .menge(menge)
                    .datum(datum)
                    .buchungstyp(buchungstyp.trim())
                    .build());
            erfolgreich++;
        }

        String status = bestimmeStatus(gesamt, fehlerhaft);
        speichereImportLog(dateiname, hash, "AUSGANG", status, gesamt, erfolgreich, fehlerhaft,
                fehlerDetails.isEmpty() ? null : String.join("; ", fehlerDetails));
        verschiebeInOrdner(file, statusZuOrdner(status));
        log.info("Ausgang-Import abgeschlossen: {} – Status={}, OK={}, Fehler={}", dateiname, status, erfolgreich, fehlerhaft);
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
