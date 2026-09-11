package com.lagermanagement.space.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BestellungCreateRequest(
        @Min(value = 1, message = "Bestellmenge muss mindestens 1 sein")
        int bestellmenge,

        @DecimalMin(value = "0.0", inclusive = false, message = "Einkaufspreis muss größer als 0 sein")
        BigDecimal einkaufspreis,

        LocalDate gewuenschtesLieferdatum,

        String notiz
) {}
