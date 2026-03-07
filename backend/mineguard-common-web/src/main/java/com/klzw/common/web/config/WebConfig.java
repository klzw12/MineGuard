package com.klzw.common.web.config;

import com.klzw.common.web.properties.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final WebProperties webProperties;

    public WebConfig(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        WebProperties.Cors cors = webProperties.getCors();
        registry.addMapping("/**")
                .allowedOriginPatterns(cors.getAllowedOrigins().split(","))
                .allowedMethods(cors.getAllowedMethods().split(","))
                .allowedHeaders(cors.getAllowedHeaders().split(","))
                .allowCredentials(cors.isAllowCredentials())
                .maxAge(cors.getMaxAge());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

}
