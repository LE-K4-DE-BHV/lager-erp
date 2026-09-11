package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.service.OrderService;
import com.lagermanagement.space.web.dto.BestellungCreateRequest;
import com.lagermanagement.space.web.dto.BestellungDto;
import com.lagermanagement.space.web.dto.BestellungResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BestellungController {

    private final OrderService orderService;

    @PostMapping("/api/v1/bestellungen/schnell/{vorschlagId}")
    public BestellungResponse schnellbestellung(@PathVariable Long vorschlagId) {
        return orderService.erstelleSchnellbestellung(vorschlagId);
    }

    @PostMapping("/api/v1/bestellungen/bearbeitet/{vorschlagId}")
    public BestellungResponse bearbeiteteBestellung(
            @PathVariable Long vorschlagId,
            @Valid @RequestBody BestellungCreateRequest request) {
        return orderService.erstelleBearbeiteteBestellung(vorschlagId, request);
    }

    @PutMapping("/api/v1/bestellvorschlaege/{vorschlagId}/ignorieren")
    public ResponseEntity<Void> ignoriereVorschlag(@PathVariable Long vorschlagId) {
        orderService.ignoriereVorschlag(vorschlagId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/bestellungen")
    public List<BestellungDto> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/api/v1/bestellungen/{id}")
    public BestellungDto findById(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
