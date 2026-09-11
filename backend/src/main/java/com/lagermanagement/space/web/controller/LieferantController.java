package com.lagermanagement.space.web.controller;

import com.lagermanagement.space.service.LieferantService;
import com.lagermanagement.space.web.dto.LieferantDto;
import com.lagermanagement.space.web.dto.LieferantUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lieferanten")
@RequiredArgsConstructor
public class LieferantController {

    private final LieferantService lieferantService;

    @GetMapping
    public List<LieferantDto> findAll() {
        return lieferantService.findAll();
    }

    @GetMapping("/{id}")
    public LieferantDto findById(@PathVariable Long id) {
        return lieferantService.findById(id);
    }

    @PutMapping("/{id}")
    public LieferantDto update(@PathVariable Long id, @Valid @RequestBody LieferantUpdateRequest request) {
        return lieferantService.update(id, request);
    }
}
