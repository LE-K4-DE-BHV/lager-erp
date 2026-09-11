package com.lagermanagement.space.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bindet app.files.* aus der application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.files")
public class AppFilesProperties {

    private String basePath = "/app/files";
}
