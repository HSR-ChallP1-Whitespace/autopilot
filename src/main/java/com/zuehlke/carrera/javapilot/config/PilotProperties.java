package com.zuehlke.carrera.javapilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Pilot Properties loaded from /resources/application.yml
 */
@ConfigurationProperties(prefix="javapilot")
public class PilotProperties {
    private String relayUrl;
    private String name;
    private String accessCode;
    private String rabbitUrl;

    public String getRelayUrl() {
        return relayUrl;
    }
    public String getName() {
        return name;
    }

    public void setRelayUrl(String relayUrl) {
        this.relayUrl = relayUrl;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAccessCode() {
        return accessCode;
    }
    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getRabbitUrl() {
        return rabbitUrl;
    }

    public void setRabbitUrl(String rabbitUrl) {
        this.rabbitUrl = rabbitUrl;
    }
}
