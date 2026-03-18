package com.klzw.service.trip.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "trip")
public class TripServiceConfig {

    private TrackConfig track = new TrackConfig();

    @Data
    public static class TrackConfig {
        private int uploadInterval = 30;
        private int maxTrackPoints = 1000;
    }
}
