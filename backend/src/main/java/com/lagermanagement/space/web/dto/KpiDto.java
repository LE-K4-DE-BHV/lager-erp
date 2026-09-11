package com.lagermanagement.space.web.dto;

import java.time.LocalDateTime;

public record KpiDto(
        int offeneVorschlaege,
        int kritischeVorschlaege,
        LocalDateTime letzterReorderLauf
) {}
