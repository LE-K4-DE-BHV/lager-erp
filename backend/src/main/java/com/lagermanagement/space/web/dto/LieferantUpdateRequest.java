package com.lagermanagement.space.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LieferantUpdateRequest(
        @NotBlank(message = "Name darf nicht leer sein")
        @Size(max = 200, message = "Name darf maximal 200 Zeichen haben")
        String name,

        @Email(message = "Ungültige E-Mail-Adresse")
        @Size(max = 200, message = "E-Mail darf maximal 200 Zeichen haben")
        String kontaktEmail,

        @Size(max = 50, message = "Telefonnummer darf maximal 50 Zeichen haben")
        String kontaktTelefon,

        @Min(value = 1, message = "Lead Time muss mindestens 1 Tag betragen")
        int leadTimeTage
) {}
