package com.klzw.service.gateway.filter;

import com.klzw.common.auth.util.JwtUtils;
import com.klzw.service.gateway.AbstractIntegrationTest;
import com.klzw.service.gateway.properties.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway Token 过滤器集成测试
 * 测试优先级：高 - 核心认证功能
 * 注意：此测试需要真实的 Nacos 和 Redis 环境
 */
@DisplayName("Gateway Token 过滤器集成测试")
public class AuthGlobalFilterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private GatewayProperties gatewayProperties;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ROLE = "ROLE_USER";

    @BeforeEach
    void setUpData() {
        // 打印配置信息用于调试
        System.out.println("=== GatewayProperties Configuration ===");
        System.out.println("IgnoreAuth paths: " + gatewayProperties.getIgnoreAuth().getPaths());
        System.out.println("Is /api/user/current ignored: " + gatewayProperties.getIgnoreAuth().isIgnored("/api/user/current"));
        System.out.println("Is /api/auth/login ignored: " + gatewayProperties.getIgnoreAuth().isIgnored("/api/auth/login"));
        
        // 清理 Redis 中的测试数据
        redisTemplate.keys("jwt:blacklist:*")
                .flatMap(redisTemplate::delete)
                .collectList()
                .block(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("测试无 Token 访问受保护接口 - 应该返回 401")
    void testAccessProtectedWithoutToken() {
        var response = webTestClient.get()
                .uri("/api/user/current")
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /api/user/current without token ===");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + new String(response.getResponseBody()));
        
        assertThat(response.getStatus().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试无效 Token 访问 - 应该返回 401")
    void testAccessWithInvalidToken() {
        var response = webTestClient.get()
                .uri("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /api/user/current with invalid token ===");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + new String(response.getResponseBody()));
        
        assertThat(response.getStatus().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试有效 Token 访问 - 应该转发到后端服务")
    void testAccessWithValidToken() {
        // 生成有效 Token
        String token = jwtUtils.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE);
        System.out.println("=== Generated token: " + token + " ===");

        var response = webTestClient.get()
                .uri("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /api/user/current with valid token ===");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + new String(response.getResponseBody()));
        
        // 可能是 502/503/504（后端服务不可用）或 200（服务可用）
        // 只要不是 401，说明 Token 验证通过了
        assertThat(response.getStatus().value())
                .as("Token 验证应该通过，不应该返回 401")
                .isNotEqualTo(401);
    }

    @Test
    @DisplayName("测试黑名单 Token 访问 - 应该返回 401")
    void testAccessWithBlacklistedToken() {
        // 生成 Token
        String token = jwtUtils.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE);
        
        // 将 Token 加入黑名单
        String blacklistKey = "jwt:blacklist:" + token;
        redisTemplate.opsForValue()
                .set(blacklistKey, "logout", Duration.ofSeconds(86400))
                .block(Duration.ofSeconds(5));

        var response = webTestClient.get()
                .uri("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /api/user/current with blacklisted token ===");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + new String(response.getResponseBody()));
        
        assertThat(response.getStatus().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试免认证路径访问 - 不需要 Token")
    void testAccessPublicPath() {
        var response = webTestClient.get()
                .uri("/api/auth/login")
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /api/auth/login (public path) ===");
        System.out.println("Status: " + response.getStatus());
        
        // 只要不是 401，说明免认证配置生效
        assertThat(response.getStatus().value())
                .as("免认证路径不应该返回 401")
                .isNotEqualTo(401);
    }

    @Test
    @DisplayName("测试 Swagger 文档路径免认证")
    void testSwaggerPathIsPublic() {
        var response = webTestClient.get()
                .uri("/swagger-ui/index.html")
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /swagger-ui/index.html ===");
        System.out.println("Status: " + response.getStatus());
        
        assertThat(response.getStatus().value())
                .as("Swagger UI 路径应该是免认证的")
                .isNotEqualTo(401);
    }

    @Test
    @DisplayName("测试 Actuator 路径免认证")
    void testActuatorPathIsPublic() {
        var response = webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectBody()
                .returnResult();
        
        System.out.println("=== Response for /actuator/health ===");
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + new String(response.getResponseBody()));
        
        assertThat(response.getStatus().value())
                .as("Actuator 路径应该是免认证的")
                .isNotEqualTo(401);
    }
}
