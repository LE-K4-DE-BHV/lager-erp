package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.service.ArtikelService;
import com.lagermanagement.space.web.dto.ArtikelCreateRequest;
import com.lagermanagement.space.web.dto.ArtikelDto;
import com.lagermanagement.space.web.dto.ArtikelUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/artikel")
@RequiredArgsConstructor
public class ArtikelController {

    private final ArtikelService artikelService;

    @GetMapping
    public List<ArtikelDto> findAll() {
        return artikelService.findAll();
    }

    @GetMapping("/{id}")
    public ArtikelDto findById(@PathVariable Long id) {
        return artikelService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ArtikelDto> create(@Valid @RequestBody ArtikelCreateRequest request) {
        ArtikelDto erstellt = artikelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(erstellt);
    }

    @PutMapping("/{id}")
    public ArtikelDto update(@PathVariable Long id, @Valid @RequestBody ArtikelUpdateRequest request) {
        return artikelService.update(id, request);
    }

    @PutMapping("/{id}/deaktivieren")
    public ArtikelDto deaktivieren(@PathVariable Long id) {
        return artikelService.deaktivieren(id);
    }

    @GetMapping("/{id}/bestand")
    public Map<String, Integer> getBestand(@PathVariable Long id) {
        return Map.of("aktuellerBestand", artikelService.getBestand(id));
    }
}
