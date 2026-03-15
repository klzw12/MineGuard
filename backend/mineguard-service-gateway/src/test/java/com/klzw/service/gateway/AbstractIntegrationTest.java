package com.klzw.service.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Tag;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 抽象集成测试基类
 * 用于需要真实外部服务（Nacos、Redis）的集成测试
 */
@SpringBootTest(
    classes = GatewayTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@ActiveProfiles("test")
@Tag("integration")
public abstract class AbstractIntegrationTest {
    
    protected WebTestClient webTestClient;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }
}
