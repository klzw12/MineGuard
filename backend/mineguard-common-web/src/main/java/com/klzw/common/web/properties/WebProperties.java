package com.klzw.common.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.web")
public class WebProperties {

    private boolean enabled = true;

    private final Cors cors = new Cors();

    private final FileUpload fileUpload = new FileUpload();

    @Data
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "Content-Type,Authorization";
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    @Data
    public static class FileUpload {
        private String maxFileSize = "50MB";
        private String maxRequestSize = "100MB";
        private String location = "${java.io.tmpdir}";
    }
}
