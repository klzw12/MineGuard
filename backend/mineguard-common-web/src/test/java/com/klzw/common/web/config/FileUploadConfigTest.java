package com.klzw.common.web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartResolver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FileUploadConfig 测试类
 */
@SpringJUnitConfig
@DisplayName("FileUploadConfig 测试")
public class FileUploadConfigTest {
    
    @Test
    @DisplayName("测试 MultipartResolver 初始化")
    public void testMultipartResolverInitialization() {
        FileUploadConfig fileUploadConfig = new FileUploadConfig();
        MultipartResolver multipartResolver = fileUploadConfig.multipartResolver();
        assertNotNull(multipartResolver);
    }
    
}
