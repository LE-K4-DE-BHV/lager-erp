package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.service.DashboardService;
import com.lagermanagement.space.service.ReorderAnalysisService;
import com.lagermanagement.space.web.dto.BestellvorschlagDto;
import com.lagermanagement.space.web.dto.KpiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ReorderAnalysisService reorderAnalysisService;

    @GetMapping("/api/v1/dashboard/kpis")
    public KpiDto getKpis() {
        return dashboardService.getKpis();
    }

    @GetMapping("/api/v1/dashboard/vorschlaege")
    public List<BestellvorschlagDto> getVorschlaege() {
        return dashboardService.getVorschlaege();
    }

    @PostMapping("/api/v1/reorder/trigger")
    public ResponseEntity<Map<String, Object>> triggerReorder() {
        int neueVorschlaege = reorderAnalysisService.runAnalysis();
        return ResponseEntity.ok(Map.of(
                "message", "Analyse abgeschlossen",
                "neueVorschlaege", neueVorschlaege
        ));
    }
}
