package com.klzw.common.map.config;

import com.klzw.common.map.properties.GaodeMapProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ConditionalOnProperty(prefix = "mineguard.map.gaode", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GaodeMapProperties.class)
public class MapAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate mapRestTemplate(GaodeMapProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return new RestTemplate(factory);
    }
}
