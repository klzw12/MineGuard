package com.klzw.service.user;

import com.klzw.common.core.config.DotenvInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 环境变量加载测试
 */
@SpringBootTest(classes = MineguardUserServiceApplication.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("环境变量加载测试")
public class EnvLoadTest {

    @Test
    @DisplayName("测试环境变量是否正确加载")
    public void testEnvVariablesLoaded() {
        String mysqlHost = System.getProperty("MYSQL_HOST");
        String mysqlPassword = System.getProperty("MYSQL_PASSWORD");
        
        System.out.println("MYSQL_HOST: " + mysqlHost);
        System.out.println("MYSQL_PASSWORD: " + mysqlPassword);
        
        assertNotNull(mysqlHost, "MYSQL_HOST 环境变量未加载");
        assertEquals("localhost", mysqlHost, "MYSQL_HOST 值不正确");
        
        assertNotNull(mysqlPassword, "MYSQL_PASSWORD 环境变量未加载");
        // 不验证密码的具体值，因为它是从.env文件加载的
    }
}
