package com.klzw.service.gateway.filter;

import com.klzw.common.auth.util.JwtUtils;
import com.klzw.service.gateway.properties.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthGlobalFilter 切片测试
 * 测试优先级：高 - 核心认证功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthGlobalFilter 认证过滤器测试")
class AuthGlobalFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private GatewayProperties gatewayProperties;

    @Mock
    private GatewayFilterChain chain;

    private AuthGlobalFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthGlobalFilter(gatewayProperties, jwtUtils);
        
        GatewayProperties.IgnoreAuth ignoreAuth = new GatewayProperties.IgnoreAuth();
        ignoreAuth.setPaths(Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/public/",
            "/actuator/",
            "/swagger-ui/",
            "/v3/api-docs/"
        ));
        when(gatewayProperties.getIgnoreAuth()).thenReturn(ignoreAuth);
    }

    @Test
    @DisplayName("测试免认证路径 - 登录接口")
    void testIgnoreAuthPath_Login() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("测试免认证路径 - Swagger UI")
    void testIgnoreAuthPath_Swagger() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui/index.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("测试免认证路径 - Actuator")
    void testIgnoreAuthPath_Actuator() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("测试无 Token 访问受保护接口 - 应该返回 401")
    void testNoToken_ReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("测试无效 Token - 应该返回 401")
    void testInvalidToken_ReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtils.validateToken("invalid_token")).thenReturn(false);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("测试有效 Token - 应该通过过滤器")
    void testValidToken_PassFilter() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid_token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtils.validateToken("valid_token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid_token")).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken("valid_token")).thenReturn("testuser");
        when(jwtUtils.getRoleFromToken("valid_token")).thenReturn("ROLE_USER");
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
        verify(jwtUtils).validateToken("valid_token");
        verify(jwtUtils).getUserIdFromToken("valid_token");
    }

    @Test
    @DisplayName("测试过滤器顺序")
    void testFilterOrder() {
        assert filter.getOrder() == 0;
    }
}
