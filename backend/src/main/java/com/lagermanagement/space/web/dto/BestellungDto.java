package com.lagermanagement.space.web.dto;

import com.lagermanagement.space.domain.enums.BestellungStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BestellungDto(
        Long id,
        String bestellnummer,
        Long artikelId,
        String artikelnummer,
        String lieferantName,
        int bestellmenge,
        BigDecimal einkaufspreis,
        LocalDate gewuenschtesLieferdatum,
        String notiz,
        BestellungStatus status,
        String erstelltVon,
        LocalDateTime erstelltAm
) {}
