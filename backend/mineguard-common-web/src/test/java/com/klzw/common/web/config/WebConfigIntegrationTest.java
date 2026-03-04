package com.klzw.common.web.config;

import com.klzw.common.web.TestWebApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.MultipartResolver;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestWebApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Web配置集成测试")
public class WebConfigIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CorsConfig corsConfig;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Autowired
    private WebConfig webConfig;

    @Test
    @DisplayName("测试CorsConfig Bean加载")
    public void testCorsConfigBeanLoaded() {
        assertNotNull(corsConfig);
        CorsFilter corsFilter = applicationContext.getBean(CorsFilter.class);
        assertNotNull(corsFilter);
    }

    @Test
    @DisplayName("测试FileUploadConfig Bean加载")
    public void testFileUploadConfigBeanLoaded() {
        assertNotNull(fileUploadConfig);
        MultipartResolver multipartResolver = applicationContext.getBean(MultipartResolver.class);
        assertNotNull(multipartResolver);
    }

    @Test
    @DisplayName("测试WebConfig Bean加载")
    public void testWebConfigBeanLoaded() {
        assertNotNull(webConfig);
    }
}
