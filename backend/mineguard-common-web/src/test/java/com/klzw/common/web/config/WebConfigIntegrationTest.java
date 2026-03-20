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

import jakarta.servlet.MultipartConfigElement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestWebApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Web配置集成测试")
public class WebConfigIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebAutoConfiguration webAutoConfiguration;

    @Autowired
    private UploadAutoConfiguration uploadAutoConfiguration;

    @Autowired
    private WebMvcConfig webMvcConfig;

    @Test
    @DisplayName("测试WebAutoConfiguration Bean加载")
    public void testWebAutoConfigurationBeanLoaded() {
        assertNotNull(webAutoConfiguration);
        CorsFilter corsFilter = applicationContext.getBean(CorsFilter.class);
        assertNotNull(corsFilter);
    }

    @Test
    @DisplayName("测试UploadAutoConfiguration Bean加载")
    public void testUploadAutoConfigurationBeanLoaded() {
        assertNotNull(uploadAutoConfiguration);
        MultipartConfigElement multipartConfig = applicationContext.getBean(MultipartConfigElement.class);
        assertNotNull(multipartConfig);
    }

    @Test
    @DisplayName("测试WebMvcConfig Bean加载")
    public void testWebMvcConfigBeanLoaded() {
        assertNotNull(webMvcConfig);
    }
}
