package com.klzw.common.auth;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试用Spring Boot应用配置类
 */
@SpringBootApplication(scanBasePackages = {
    "com.klzw.common.auth",
    "com.klzw.common.redis",
    "com.klzw.common.core"
})
public class TestAuthApplication {
}
