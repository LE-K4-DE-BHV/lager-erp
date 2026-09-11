package com.lagermanagement.space.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindet app.reorder.* aus der application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.reorder")
public class AppReorderProperties {

    private int consumptionPeriodDays = 30;
    private String cronExpression = "0 0 2 * * *";
}
