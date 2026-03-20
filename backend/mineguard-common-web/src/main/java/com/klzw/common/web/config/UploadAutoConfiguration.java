package com.klzw.common.web.config;

import com.klzw.common.web.properties.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;


import jakarta.servlet.MultipartConfigElement;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(UploadProperties.class)
public class UploadAutoConfiguration {

    private final UploadProperties uploadProperties;

    public UploadAutoConfiguration(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        DataSize maxFileSize = DataSize.parse(uploadProperties.getMaxFileSize());
        DataSize maxRequestSize = DataSize.parse(uploadProperties.getMaxRequestSize());
        factory.setMaxFileSize(maxFileSize);
        factory.setMaxRequestSize(maxRequestSize);
        log.info("文件上传配置: maxFileSize={}, maxRequestSize={}", maxFileSize, maxRequestSize);
        return factory.createMultipartConfig();
    }
}