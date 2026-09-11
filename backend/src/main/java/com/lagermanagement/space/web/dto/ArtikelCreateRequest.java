package com.lagermanagement.space.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ArtikelCreateRequest(
        @NotBlank(message = "Artikelnummer darf nicht leer sein")
        @Size(max = 50, message = "Artikelnummer darf maximal 50 Zeichen haben")
        String artikelnummer,

        @NotBlank(message = "Bezeichnung darf nicht leer sein")
        @Size(max = 200, message = "Bezeichnung darf maximal 200 Zeichen haben")
        String bezeichnung,

        @NotBlank(message = "Mengeneinheit darf nicht leer sein")
        @Size(max = 20, message = "Mengeneinheit darf maximal 20 Zeichen haben")
        String mengeneinheit,

        @NotBlank(message = "Warengruppe darf nicht leer sein")
        @Size(max = 100, message = "Warengruppe darf maximal 100 Zeichen haben")
        String warengruppe,

        Long lieferantId,

        @Min(value = 0, message = "Sicherheitsbestand darf nicht negativ sein")
        int sicherheitsbestand,

        @Min(value = 0, message = "Bestellpunkt darf nicht negativ sein")
        int bestellpunkt,

        @Min(value = 1, message = "Standardbestellmenge muss mindestens 1 sein")
        int standardBestellmenge,

        @NotNull(message = "Einkaufspreis ist Pflicht")
        @DecimalMin(value = "0.0", inclusive = false, message = "Einkaufspreis muss größer als 0 sein")
        BigDecimal einkaufspreis
) {}
