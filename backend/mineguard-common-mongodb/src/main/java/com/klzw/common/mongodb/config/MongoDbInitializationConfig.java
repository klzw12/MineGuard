package com.klzw.common.mongodb.config;

import com.klzw.common.mongodb.util.TtlIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * MongoDB 初始化配置类
 * 用于在应用启动时初始化 MongoDB 相关的配置，包括 TTL 索引的创建
 */
@Slf4j
@Component
public class MongoDbInitializationConfig implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public MongoDbInitializationConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 应用启动时执行初始化操作
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        initTtlIndexes();
    }

    /**
     * 初始化 TTL 索引
     */
    private void initTtlIndexes() {
        try {
            TtlIndexUtil.initAllTtlIndexes(mongoTemplate);
            log.info("MongoDB TTL索引初始化完成");
        } catch (Exception e) {
            log.error("初始化TTL索引失败，但不影响应用启动", e);
        }
    }

}