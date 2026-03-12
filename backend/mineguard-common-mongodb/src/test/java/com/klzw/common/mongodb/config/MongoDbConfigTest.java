package com.klzw.common.mongodb.config;

import com.klzw.common.mongodb.properties.MongoDbProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDbAutoConfiguration 配置类测试
 */
@DisplayName("MongoDbAutoConfiguration MongoDB配置测试")
class MongoDbConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MongoDbAutoConfiguration.class))
            .withPropertyValues("mineguard.mongodb.database=testdb");

    @Test
    @DisplayName("MongoDbAutoConfiguration应自动配置")
    void mongoDbAutoConfiguration_shouldAutoConfigure() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MongoDbAutoConfiguration.class);
        });
    }

    @Test
    @DisplayName("MongoDbAutoConfiguration应提供MongoTemplate")
    void mongoDbAutoConfiguration_shouldProvideMongoTemplate() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("mongoTemplate");
        });
    }

    @Test
    @DisplayName("MongoDbAutoConfiguration配置类应正常加载")
    void mongoDbAutoConfiguration_classShouldLoad() {
        // 验证配置类可以正常实例化
        MongoDbProperties properties = new MongoDbProperties();
        properties.setDatabase("testdb");
        MongoDbAutoConfiguration config = new MongoDbAutoConfiguration(properties);
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("MongoDbInitializationConfig配置类应正常加载")
    void mongoDbInitializationConfig_classShouldLoad() {
        // 验证初始化配置类可以正常实例化
        MongoDbInitializationConfig config = new MongoDbInitializationConfig(null);
        assertThat(config).isNotNull();
    }
}