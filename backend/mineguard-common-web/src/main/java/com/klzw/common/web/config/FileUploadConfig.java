package com.klzw.common.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * 文件上传配置类
 * 用于配置文件上传限制和路径
 * 在 Spring Boot 3+ 中，使用 application.yml 中的 spring.servlet.multipart 配置
 */
@Configuration
public class FileUploadConfig {
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
}
