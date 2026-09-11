package com.lagermanagement.space.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Bindet app.security.* aus der application.yml.
 * @NotEmpty auf operatorPasswordHash erzwingt Fail Fast beim Start,
 * wenn APP_OPERATOR_PASSWORD_HASH nicht oder leer gesetzt ist.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    @NotEmpty(message = "APP_OPERATOR_PASSWORD_HASH darf nicht leer oder nicht gesetzt sein")
    private String operatorPasswordHash;
}
