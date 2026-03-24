package com.klzw.common.mongodb.config;

import com.klzw.common.mongodb.repository.BaseMongoRepository;
import com.klzw.common.mongodb.repository.BaseMongoRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB Repository 配置类
 */
@Configuration
@EnableMongoRepositories(
    basePackages = "com.klzw",
    repositoryBaseClass = BaseMongoRepositoryImpl.class
)
public class MongoRepositoryConfig {

}