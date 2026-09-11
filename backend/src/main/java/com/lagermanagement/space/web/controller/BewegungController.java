package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.service.InventoryService;
import com.lagermanagement.space.web.dto.AusgangRequest;
import com.lagermanagement.space.web.dto.BewegungResponse;
import com.lagermanagement.space.web.dto.EingangRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bewegungen")
@RequiredArgsConstructor
public class BewegungController {

    private final InventoryService inventoryService;

    @PostMapping("/eingang")
    public BewegungResponse bucheEingang(@Valid @RequestBody EingangRequest request) {
        return inventoryService.bucheEingang(request);
    }

    @PostMapping("/eingang/{transaktionId}/bestellung-abschliessen")
    public ResponseEntity<Void> bestellungAbschliessen(@PathVariable Long transaktionId) {
        inventoryService.bestellungAbschliessen(transaktionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ausgang")
    public ResponseEntity<Void> bucheAusgang(@Valid @RequestBody AusgangRequest request) {
        inventoryService.bucheAusgang(request);
        return ResponseEntity.ok().build();
    }
}
