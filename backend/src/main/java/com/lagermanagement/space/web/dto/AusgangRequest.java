package com.lagermanagement.space.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AusgangRequest(
        @NotNull(message = "Artikel-ID ist Pflicht")
        Long artikelId,

        @Min(value = 1, message = "Menge muss mindestens 1 sein")
        int menge,

        @NotNull(message = "Datum ist Pflicht")
        LocalDate datum,

        @NotBlank(message = "Buchungstyp ist Pflicht")
        String buchungstyp,

        String grund
) {}
