package com.klzw.common.mongodb.config;

import com.klzw.common.mongodb.properties.MongoDbProperties;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB 配置类
 * 用于配置 MongoTemplate 和连接池
 * <p>
 * 异常处理策略通过 {@link com.klzw.common.mongodb.handler.MongoDbExceptionHandlerStrategy} 自动注册
 */
@Configuration
@EnableConfigurationProperties(MongoDbProperties.class)
public class MongoDbConfig {

    private final MongoDbProperties mongoDbProperties;

    public MongoDbConfig(MongoDbProperties mongoDbProperties) {
        this.mongoDbProperties = mongoDbProperties;
    }

    /**
     * 创建 MongoClient
     * @return MongoClient 实例
     */
    @Bean
    public MongoClient mongoClient() {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoDbProperties.getConnectionString()))
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(mongoDbProperties.getMaxConnections())
                        .minSize(mongoDbProperties.getMinConnectionsPerHost())
                        .maxWaitTime(mongoDbProperties.getMaxWaitTime(), TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(1, TimeUnit.HOURS)
                )
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(mongoDbProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(mongoDbProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
                )
                .applyToServerSettings(builder -> builder
                        .heartbeatFrequency(mongoDbProperties.getHeartbeatFrequency(), TimeUnit.MILLISECONDS)
                )
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    /**
     * 创建 MongoDatabaseFactory
     * @param mongoClient MongoClient 实例
     * @return MongoDatabaseFactory 实例
     */
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoDbProperties.getDatabase());
    }

    /**
     * 创建 MongoTemplate
     * @param mongoDatabaseFactory MongoDatabaseFactory 实例
     * @return MongoTemplate 实例
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

}
