package com.lagermanagement.space.web.dto;

public record BestellungResponse(
        Long bestellungId,
        String bestellnummer,
        String pdfPfad,
        String xmlPfad
) {}
