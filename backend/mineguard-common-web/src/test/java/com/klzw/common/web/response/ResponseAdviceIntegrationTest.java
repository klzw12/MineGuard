package com.klzw.common.web.response;

import com.klzw.common.core.result.Result;
import com.klzw.common.web.TestWebApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestWebApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("ResponseAdvice 集成测试")
public class ResponseAdviceIntegrationTest {

    @Autowired
    private ResponseAdvice responseAdvice;

    @Test
    @DisplayName("测试ResponseAdvice Bean加载")
    public void testResponseAdviceBeanLoaded() {
        assertNotNull(responseAdvice);
    }

    @Test
    @DisplayName("测试supports方法")
    public void testSupportsMethod() {
        boolean result = responseAdvice.supports(null, null);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试Result对象结构")
    public void testResultObjectStructure() {
        Result<String> result = Result.success("testData");
        assertTrue(result instanceof Result);
        assertEquals(200, result.getCode());
        assertEquals("testData", result.getData());
    }
}
