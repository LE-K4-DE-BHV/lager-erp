package com.lagermanagement.space.web.dto;

public record BewegungResponse(
        Long transaktionId,
        boolean bestellungKannGeschlossenWerden
) {}
