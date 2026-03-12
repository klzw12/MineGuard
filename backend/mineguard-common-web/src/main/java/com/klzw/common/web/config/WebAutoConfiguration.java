package com.klzw.common.web.config;


import com.klzw.common.web.properties.WebProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "mineguard.web", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebProperties.class)
public class WebAutoConfiguration implements WebMvcConfigurer {

    static {
        log.info("WebAutoConfiguration initialized");
    }

    private final WebProperties webProperties;

    public WebAutoConfiguration(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    // 注意：CORS配置优先在网关处理，此处保留corsFilter作为备份

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter() {
        WebProperties.Cors cors = webProperties.getCors();

        CorsConfiguration config = new CorsConfiguration();

        if (cors.getAllowedOrigins() != null && !cors.getAllowedOrigins().isEmpty()) {
            config.addAllowedOriginPattern(cors.getAllowedOrigins());
        }

        config.addAllowedMethod(cors.getAllowedMethods());
        config.addAllowedHeader(cors.getAllowedHeaders());
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }


}
