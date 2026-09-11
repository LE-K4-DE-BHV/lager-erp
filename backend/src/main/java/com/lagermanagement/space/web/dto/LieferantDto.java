package com.lagermanagement.space.web.dto;

public record LieferantDto(
        Long id,
        String lieferantId,
        String name,
        String kontaktEmail,
        String kontaktTelefon,
        int leadTimeTage
) {}
