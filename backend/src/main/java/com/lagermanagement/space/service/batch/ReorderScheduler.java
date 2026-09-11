package com.lagermanagement.space.service.batch;

import com.lagermanagement.space.service.BerichtService;
import com.lagermanagement.space.service.ReorderAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nächtlicher Reorder- und Berichts-Scheduler.
 * Fehler in einem Lauf unterbrechen den nächsten geplanten Lauf nicht.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReorderScheduler {

    private final ReorderAnalysisService reorderAnalysisService;
    private final BerichtService berichtService;

    @Scheduled(cron = "${app.reorder.cron-expression}")
    public void scheduledReorder() {
        log.info("Reorder-Analyse gestartet (scheduled)");
        try {
            int neueVorschlaege = reorderAnalysisService.runAnalysis();
            log.info("Reorder-Analyse abgeschlossen – neue Vorschläge: {}", neueVorschlaege);
        } catch (Exception e) {
            log.error("Fehler in der Reorder-Analyse: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "${app.report.cron-expression}")
    public void scheduledReport() {
        log.info("Bestandsbericht-Generierung gestartet (scheduled)");
        try {
            berichtService.generateDailyReport();
        } catch (Exception e) {
            log.error("Fehler bei der Berichtsgenerierung: {}", e.getMessage());
        }
    }
}
