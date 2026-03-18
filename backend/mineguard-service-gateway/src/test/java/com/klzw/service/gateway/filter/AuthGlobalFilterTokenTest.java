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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthGlobalFilter Token验证集成测试（使用Mock）
 * 测试优先级：高 - 核心认证功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthGlobalFilter Token验证测试")
class AuthGlobalFilterTokenTest {

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
    @DisplayName("测试有效Token - 应该放行")
    void testValidToken_ShouldPass() {
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
    }

    @Test
    @DisplayName("测试无效Token - 应该返回401")
    void testInvalidToken_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtils.validateToken("invalid_token")).thenReturn(false);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtils).validateToken("invalid_token");
    }

    @Test
    @DisplayName("测试无Token访问受保护路径 - 应该返回401")
    void testNoToken_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtils, never()).validateToken(any());
    }

    @Test
    @DisplayName("测试黑名单Token - 应该返回401")
    void testBlacklistedToken_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/user/current")
                .header(HttpHeaders.AUTHORIZATION, "Bearer blacklisted_token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtils.validateToken("blacklisted_token")).thenReturn(false);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(jwtUtils).validateToken("blacklisted_token");
    }

    @Test
    @DisplayName("测试免认证路径 - 不验证Token")
    void testIgnoreAuthPath_ShouldSkipValidation() {
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
}
