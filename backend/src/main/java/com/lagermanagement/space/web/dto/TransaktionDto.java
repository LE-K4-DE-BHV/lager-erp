package com.lagermanagement.space.web.dto;

import com.lagermanagement.space.domain.enums.TransaktionQuelle;
import com.lagermanagement.space.domain.enums.TransaktionTyp;

import java.time.LocalDate;

public record TransaktionDto(
        Long id,
        String artikelnummer,
        String artikelBezeichnung,
        TransaktionTyp typ,
        String buchungstyp,
        int menge,
        LocalDate datum,
        TransaktionQuelle quelle,
        String benutzerId,
        String bestellnummer
) {}
