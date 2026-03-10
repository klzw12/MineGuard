package com.klzw.common.mongodb.config;

import com.klzw.common.mongodb.properties.MongoDbProperties;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@ConditionalOnClass(name = "com.mongodb.client.MongoClient")
@ConditionalOnProperty(prefix = "mineguard.mongodb", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MongoDbProperties.class)
public class MongoDbAutoConfiguration {

    private final MongoDbProperties mongoDbProperties;

    public MongoDbAutoConfiguration(MongoDbProperties mongoDbProperties) {
        this.mongoDbProperties = mongoDbProperties;
    }

    @Bean
    @ConditionalOnMissingBean
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

    @Bean
    @ConditionalOnMissingBean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongoDbProperties.getDatabase());
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}
