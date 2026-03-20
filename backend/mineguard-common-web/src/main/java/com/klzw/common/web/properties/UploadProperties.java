package com.klzw.common.web.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.servlet.multipart")
public class UploadProperties {
    private String maxFileSize = "10MB";
    private String maxRequestSize = "20MB";
}