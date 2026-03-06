package com.klzw.common.file.service;

import com.klzw.common.file.AbstractFileIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 存储服务集成测试
 */
@DisplayName("存储服务集成测试")
@Tag("integration")
class StorageServiceIntegrationTest extends AbstractFileIntegrationTest {

    @Autowired
    private StorageService storageService;

    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile("test.png", "test.png", "image/png", "test image content".getBytes());
    }

    @Test
    @DisplayName("上传文件")
    void testUploadFile() throws IOException {
        String filePath = storageService.upload(testFile.getInputStream(), "test.png", "image/png");

        assertNotNull(filePath);
    }

    @Test
    @DisplayName("下载文件")
    void testDownloadFile() throws IOException {
        String filePath = storageService.upload(testFile.getInputStream(), "test.png", "image/png");
        assertNotNull(filePath);

        InputStream inputStream = storageService.download(filePath);

        assertNotNull(inputStream);
        inputStream.close();
    }

    @Test
    @DisplayName("删除文件")
    void testDeleteFile() throws IOException {
        String filePath = storageService.upload(testFile.getInputStream(), "test.png", "image/png");
        assertNotNull(filePath);

        boolean result = storageService.delete(filePath);

        assertTrue(result);
    }

    @Test
    @DisplayName("获取文件信息")
    void testGetFileInfo() throws IOException {
        String filePath = storageService.upload(testFile.getInputStream(), "test.png", "image/png");
        assertNotNull(filePath);

        var fileInfo = storageService.getFileInfo(filePath);

        assertNotNull(fileInfo);
    }
}
