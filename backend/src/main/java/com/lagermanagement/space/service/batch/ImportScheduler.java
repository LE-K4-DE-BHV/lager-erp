package com.lagermanagement.space.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Zentraler Import-Scheduler: orchestriert Stammdaten- und Transaktionen-Import.
 * Stammdaten laufen immer zuerst (FK-Abhängigkeit: Artikel muss vor Transaktion existieren).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImportScheduler {

    private final StammdatenImportService stammdatenImportService;
    private final TransaktionenImportService transaktionenImportService;

    @Scheduled(cron = "${app.import.cron-expression}")
    public void runImport() {
        log.info("Import-Scheduler gestartet");
        try {
            stammdatenImportService.processAll();
        } catch (Exception e) {
            log.error("Fehler im Stammdaten-Import: {}", e.getMessage());
        }
        try {
            transaktionenImportService.processAll();
        } catch (Exception e) {
            log.error("Fehler im Transaktionen-Import: {}", e.getMessage());
        }
        log.info("Import-Scheduler abgeschlossen");
    }
}
