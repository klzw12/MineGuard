package com.klzw.common.map.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.map.gaode")
public class GaodeMapProperties {
    private boolean enabled = true;
    private String apiKey;
    private String apiUrl = "https://restapi.amap.com/v3";
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
    private long cacheExpire = 3600;
}
