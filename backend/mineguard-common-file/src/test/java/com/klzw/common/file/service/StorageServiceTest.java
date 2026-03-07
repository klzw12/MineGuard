package com.klzw.common.file.service;

import com.klzw.common.file.impl.MinioStorageServiceImpl;
import com.klzw.common.file.strategy.MinioStorageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StorageService 单元测试
 * <p>
 * 使用Mockito模拟MinioStorageStrategy，测试存储服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("存储服务单元测试")
class StorageServiceTest {

    @Mock
    private MinioStorageStrategy minioStorageStrategy;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new MinioStorageServiceImpl(minioStorageStrategy);
    }

    @Test
    @DisplayName("测试上传文件 - 成功")
    void testUpload_Success() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(minioStorageStrategy.upload(any(InputStream.class), anyString(), anyString()))
                .thenReturn("test-bucket/test-file.png");

        String result = storageService.upload(inputStream, "test.png", "image/png");

        assertNotNull(result);
        assertEquals("test-bucket/test-file.png", result);
        verify(minioStorageStrategy, times(1)).upload(any(InputStream.class), eq("test.png"), eq("image/png"));
    }

    @Test
    @DisplayName("测试上传文件（指定模块和文件夹）- 成功")
    void testUploadWithModuleAndFolder_Success() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(minioStorageStrategy.upload(any(InputStream.class), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String filePath = invocation.getArgument(1);
                    return "test-bucket/" + filePath;
                });

        String result = storageService.upload("user", "avatar", inputStream, "test.png", "image/png");

        assertNotNull(result);
        assertTrue(result.contains("avatar/"));
        assertTrue(result.endsWith(".png"));
        verify(minioStorageStrategy, times(1)).upload(any(InputStream.class), contains("avatar/"), eq("image/png"));
    }

    @Test
    @DisplayName("测试下载文件 - 成功")
    void testDownload_Success() {
        InputStream expectedStream = new ByteArrayInputStream("test content".getBytes());
        when(minioStorageStrategy.download(anyString())).thenReturn(expectedStream);

        InputStream result = storageService.download("test-bucket/test-file.png");

        assertNotNull(result);
        verify(minioStorageStrategy, times(1)).download("test-bucket/test-file.png");
    }

    @Test
    @DisplayName("测试删除文件 - 成功")
    void testDelete_Success() {
        when(minioStorageStrategy.delete(anyString())).thenReturn(true);

        boolean result = storageService.delete("test-bucket/test-file.png");

        assertTrue(result);
        verify(minioStorageStrategy, times(1)).delete("test-bucket/test-file.png");
    }

    @Test
    @DisplayName("测试删除文件 - 失败")
    void testDelete_Failure() {
        when(minioStorageStrategy.delete(anyString())).thenReturn(false);

        boolean result = storageService.delete("non-existent-file.png");

        assertFalse(result);
        verify(minioStorageStrategy, times(1)).delete("non-existent-file.png");
    }

    @Test
    @DisplayName("测试获取文件URL - 成功")
    void testGetUrl_Success() {
        String expectedUrl = "http://minio:9000/test-bucket/test-file.png?signature=xxx";
        when(minioStorageStrategy.getUrl(anyString(), anyLong())).thenReturn(expectedUrl);

        String result = storageService.getUrl("test-bucket/test-file.png", 3600);

        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(minioStorageStrategy, times(1)).getUrl("test-bucket/test-file.png", 3600);
    }

    @Test
    @DisplayName("测试获取文件信息 - 成功")
    void testGetFileInfo_Success() {
        Map<String, Object> expectedInfo = new HashMap<>();
        expectedInfo.put("size", 1024L);
        expectedInfo.put("contentType", "image/png");
        when(minioStorageStrategy.getFileInfo(anyString())).thenReturn(expectedInfo);

        Map<String, Object> result = storageService.getFileInfo("test-bucket/test-file.png");

        assertNotNull(result);
        assertEquals(1024L, result.get("size"));
        assertEquals("image/png", result.get("contentType"));
        verify(minioStorageStrategy, times(1)).getFileInfo("test-bucket/test-file.png");
    }

    @Test
    @DisplayName("测试健康检查 - 健康")
    void testIsHealthy_Healthy() {
        when(minioStorageStrategy.isHealthy()).thenReturn(true);

        boolean result = storageService.isHealthy();

        assertTrue(result);
        verify(minioStorageStrategy, times(1)).isHealthy();
    }

    @Test
    @DisplayName("测试健康检查 - 不健康")
    void testIsHealthy_Unhealthy() {
        when(minioStorageStrategy.isHealthy()).thenReturn(false);

        boolean result = storageService.isHealthy();

        assertFalse(result);
        verify(minioStorageStrategy, times(1)).isHealthy();
    }
}
