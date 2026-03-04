package com.klzw.common.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebLogAspect 单元测试")
public class WebLogAspectTest {

    private WebLogAspect webLogAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    public void setUp() {
        webLogAspect = new WebLogAspect();
    }

    @Test
    @DisplayName("测试切面初始化")
    public void testWebLogAspectInitialization() {
        assertNotNull(webLogAspect);
    }

    @Test
    @DisplayName("测试环绕通知正常执行")
    public void testDoAroundWithNormalExecution() throws Throwable {
        Object expectedResult = "testResult";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"param1", "param2"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("TestController");
        when(signature.getName()).thenReturn("testMethod");

        Object result = webLogAspect.doAround(joinPoint);

        assertEquals(expectedResult, result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("测试环绕通知异常处理")
    public void testDoAroundWithException() throws Throwable {
        RuntimeException expectedException = new RuntimeException("Test exception");
        when(joinPoint.proceed()).thenThrow(expectedException);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("TestController");
        when(signature.getName()).thenReturn("testMethod");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            webLogAspect.doAround(joinPoint);
        });

        assertEquals("Test exception", thrown.getMessage());
    }
}
