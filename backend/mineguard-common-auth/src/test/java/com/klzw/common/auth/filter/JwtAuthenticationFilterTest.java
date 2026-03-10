package com.klzw.common.auth.filter;

import com.klzw.common.auth.config.JwtProperties;
import com.klzw.common.auth.context.UserContext;
import com.klzw.common.auth.util.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT 认证过滤器单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JwtProperties jwtProperties;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_PREFIX = "Bearer ";
    private static final String TEST_HEADER = "Authorization";

    @BeforeEach
    void setUp() {
        when(jwtProperties.getHeader()).thenReturn(TEST_HEADER);
        when(jwtProperties.getPrefix()).thenReturn(TEST_PREFIX);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, jwtProperties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void doFilterInternal_shouldSetUserContext_whenValidToken() throws ServletException, IOException {
        Long userId = 1L;
        String username = "testUser";
        String authHeader = TEST_PREFIX + TEST_TOKEN;

        request.addHeader(TEST_HEADER, authHeader);

        when(jwtUtils.getTokenFromHeader(authHeader)).thenReturn(TEST_TOKEN);
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(TEST_TOKEN)).thenReturn(userId);
        when(jwtUtils.getUsernameFromToken(TEST_TOKEN)).thenReturn(username);

        AtomicReference<Long> capturedUserId = new AtomicReference<>();
        AtomicReference<String> capturedUsername = new AtomicReference<>();
        FilterChain filterChain = (req, res) -> {
            capturedUserId.set(UserContext.getUserId());
            capturedUsername.set(UserContext.getUsername());
        };

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(userId, capturedUserId.get());
        assertEquals(username, capturedUsername.get());
    }

    @Test
    void doFilterInternal_shouldNotSetUserContext_whenNoAuthHeader() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    @Test
    void doFilterInternal_shouldNotSetUserContext_whenInvalidToken() throws ServletException, IOException {
        String authHeader = TEST_PREFIX + "invalid.token";
        request.addHeader(TEST_HEADER, authHeader);

        when(jwtUtils.getTokenFromHeader(authHeader)).thenReturn("invalid.token");
        when(jwtUtils.validateToken("invalid.token")).thenReturn(false);

        FilterChain filterChain = mock(FilterChain.class);

        assertThrows(com.klzw.common.auth.exception.AuthException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    void doFilterInternal_shouldNotSetUserContext_whenWrongPrefix() throws ServletException, IOException {
        String authHeader = "Basic " + TEST_TOKEN;
        request.addHeader(TEST_HEADER, authHeader);

        when(jwtUtils.getTokenFromHeader(authHeader)).thenReturn(null);

        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    @Test
    void doFilterInternal_shouldClearUserContext_afterFilterChain() throws ServletException, IOException {
        Long userId = 1L;
        String username = "testUser";
        String authHeader = TEST_PREFIX + TEST_TOKEN;

        request.addHeader(TEST_HEADER, authHeader);

        when(jwtUtils.getTokenFromHeader(authHeader)).thenReturn(TEST_TOKEN);
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(TEST_TOKEN)).thenReturn(userId);
        when(jwtUtils.getUsernameFromToken(TEST_TOKEN)).thenReturn(username);

        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }
}
