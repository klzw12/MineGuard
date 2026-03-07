package com.klzw.common.web.response;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseAdvice 单元测试")
public class ResponseAdviceTest {

    private ResponseAdvice responseAdvice;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @BeforeEach
    public void setUp() {
        responseAdvice = new ResponseAdvice();
    }

    @Test
    @DisplayName("测试supports方法返回true")
    public void testSupportsReturnsTrue() {
        boolean result = responseAdvice.supports(methodParameter, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试包装普通对象")
    public void testWrapNormalObject() {
        when(request.getURI()).thenReturn(URI.create("/api/test"));
        
        Object body = new Object();
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertNotNull(result);
        assertTrue(result instanceof Result);
    }

    @Test
    @DisplayName("测试不包装Result对象")
    public void testNotWrapResultObject() {
        when(request.getURI()).thenReturn(URI.create("/api/test"));
        
        Result<String> body = Result.success("testData");
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertSame(body, result);
    }

    @Test
    @DisplayName("测试不包装String对象")
    public void testNotWrapStringObject() {
        when(request.getURI()).thenReturn(URI.create("/api/test"));
        
        Object body = "rawString";
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertEquals("rawString", result);
    }

    @Test
    @DisplayName("测试排除Swagger路径")
    public void testExcludeSwaggerPath() {
        when(request.getURI()).thenReturn(URI.create("/swagger-ui/index.html"));
        
        Object body = "testData";
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertEquals("testData", result);
    }

    @Test
    @DisplayName("测试排除API文档路径")
    public void testExcludeApiDocsPath() {
        when(request.getURI()).thenReturn(URI.create("/v3/api-docs"));
        
        Object body = "testData";
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertEquals("testData", result);
    }

    @Test
    @DisplayName("测试排除Actuator路径")
    public void testExcludeActuatorPath() {
        when(request.getURI()).thenReturn(URI.create("/actuator/health"));
        
        Object body = "testData";
        Object result = responseAdvice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);
        
        assertEquals("testData", result);
    }
}
