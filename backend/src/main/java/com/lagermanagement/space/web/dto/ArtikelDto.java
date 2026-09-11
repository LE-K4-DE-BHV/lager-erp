package com.lagermanagement.space.web.dto;

import com.lagermanagement.space.domain.enums.ArtikelStatus;

import java.math.BigDecimal;

public record ArtikelDto(
        Long id,
        String artikelnummer,
        String bezeichnung,
        String mengeneinheit,
        String warengruppe,
        Long lieferantId,
        String lieferantName,
        int aktuellerBestand,
        int sicherheitsbestand,
        int bestellpunkt,
        int standardBestellmenge,
        BigDecimal einkaufspreis,
        ArtikelStatus status
) {}
