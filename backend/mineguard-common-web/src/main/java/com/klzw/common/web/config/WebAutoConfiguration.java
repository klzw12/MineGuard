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

    // 注意：CORS配置优先在网关处理，此处不再配置CorsFilter
    // 为了确保不会有多个CORS过滤器，我们明确不注册CorsFilter
    // @Bean
    // @ConditionalOnMissingBean
    // public CorsFilter corsFilter() {
    //     WebProperties.Cors cors = webProperties.getCors();

    //     CorsConfiguration config = new CorsConfiguration();

    //     if (cors.getAllowedOrigins() != null && !cors.getAllowedOrigins().isEmpty()) {
    //         // 处理逗号分隔的多个源
    //         for (String origin : cors.getAllowedOrigins().split(",")) {
    //             config.addAllowedOriginPattern(origin.trim());
    //         }
    //     }

    //     // 处理逗号分隔的多个方法
    //     if (cors.getAllowedMethods() != null && !cors.getAllowedMethods().isEmpty()) {
    //         for (String method : cors.getAllowedMethods().split(",")) {
    //             config.addAllowedMethod(method.trim());
    //         }
    //     }

    //     // 处理逗号分隔的多个头部
    //     if (cors.getAllowedHeaders() != null && !cors.getAllowedHeaders().isEmpty()) {
    //         for (String header : cors.getAllowedHeaders().split(",")) {
    //             config.addAllowedHeader(header.trim());
    //         }
    //     }

    //     config.setAllowCredentials(cors.isAllowCredentials());
    //     config.setMaxAge(cors.getMaxAge());

    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);

    //     return new CorsFilter(source);
    // }
    
    // 确保不会有其他CORS过滤器被注册
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                // 明确禁用CORS映射，因为CORS由网关处理
                log.info("CORS mappings disabled in WebAutoConfiguration, handled by gateway");
            }
        };
    }


}
