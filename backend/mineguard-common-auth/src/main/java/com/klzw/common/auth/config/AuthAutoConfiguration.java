package com.klzw.common.auth.config;

import com.klzw.common.auth.util.JwtUtils;
import com.klzw.common.auth.util.PasswordUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.security.crypto.password.PasswordEncoder")
@ConditionalOnProperty(prefix = "mineguard.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties.class)
public class AuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties jwtProperties, StringRedisTemplate redisTemplate) {
        return new JwtUtils(jwtProperties, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordUtils passwordUtils(PasswordEncoder passwordEncoder) {
        return new PasswordUtils(passwordEncoder);
    }
}
