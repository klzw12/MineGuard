package com.klzw.common.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.klzw.common.core.config.DotenvInitializer;

/**
 * File模块集成测试基类
 * <p>
 * 所有File集成测试的父类，提供统一的配置：
 * - 加载测试环境配置（application-test.yml）和敏感配置（application-secret.yml）
 * - 标记为集成测试（@Tag("integration")）
 * <p>
 * 子类只需继承此类即可进行集成测试
 * <p>
 * 注意：集成测试需要配置正确的OSS连接和百度AI API
 */
@SpringBootTest(classes = MineguardCommonFileApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = DotenvInitializer.class)
@Tag("integration")
@DisplayName("File模块集成测试")
public abstract class AbstractFileIntegrationTest {
    // File集成测试基类，子类继承即可
}
