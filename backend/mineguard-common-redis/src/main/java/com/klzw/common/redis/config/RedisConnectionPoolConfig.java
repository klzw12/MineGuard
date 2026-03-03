package com.klzw.common.redis.config;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * Redis 连接池配置类
 */
@Configuration
public class RedisConnectionPoolConfig {

    private final RedisProperties redisProperties;

    public RedisConnectionPoolConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * 配置 Redis 连接工厂，设置连接池参数
     *
     * @return RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 基本配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisProperties.getHost());
        redisConfig.setPort(redisProperties.getPort());
        redisConfig.setDatabase(redisProperties.getDatabase());
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            redisConfig.setPassword(redisProperties.getPassword());
        }

        // 连接池配置
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder();
        // 指定泛型类型参数，消除未检查的赋值警告
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(redisProperties.getPool().getMaxActive());
        // 使用 Duration 替代已弃用的 setMaxWaitMillis
        poolConfig.setMaxWait(Duration.ofMillis(redisProperties.getPool().getMaxWait()));
        poolConfig.setMaxIdle(redisProperties.getPool().getMaxIdle());
        poolConfig.setMinIdle(redisProperties.getPool().getMinIdle());
        builder.poolConfig(poolConfig);
        builder.commandTimeout(Duration.ofMillis(redisProperties.getTimeout()));

        // 创建连接工厂
        return new LettuceConnectionFactory(redisConfig, builder.build());
    }
}
