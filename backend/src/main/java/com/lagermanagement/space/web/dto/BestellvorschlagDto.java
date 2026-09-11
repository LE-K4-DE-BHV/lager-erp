package com.lagermanagement.space.web.dto;

import com.lagermanagement.space.domain.enums.VorschlagStatus;

import java.time.LocalDateTime;

public record BestellvorschlagDto(
        Long id,
        Long artikelId,
        String artikelnummer,
        String artikelBezeichnung,
        String lieferantName,
        int aktuellerBestand,
        int bestellpunkt,
        int sicherheitsbestand,
        int vorgeschlageneMenge,
        VorschlagStatus status,
        LocalDateTime erstelltAm,
        boolean istKritisch
) {}
