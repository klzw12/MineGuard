package com.klzw.common.file.service;

import com.klzw.common.file.impl.FileDownloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文件下载服务单元测试")
@Tag("unit")
class FileDownloadServiceTest {

    @InjectMocks
    private FileDownloadServiceImpl fileDownloadService;

    @Mock
    private StorageService storageService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("测试下载文件 - 成功")
    void testDownload() {
        InputStream mockStream = new ByteArrayInputStream("test content".getBytes());
        when(storageService.download(anyString())).thenReturn(mockStream);

        InputStream result = fileDownloadService.download("test.txt");

        assertNotNull(result);
        verify(storageService, times(1)).download(anyString());
    }

    @Test
    @DisplayName("测试获取下载URL - 成功")
    void testGetDownloadUrl() {
        when(storageService.getUrl(anyString(), anyLong())).thenReturn("http://test.url/file");

        String result = fileDownloadService.getDownloadUrl("test.txt", 3600);

        assertNotNull(result);
        assertEquals("http://test.url/file", result);
        verify(storageService, times(1)).getUrl(anyString(), anyLong());
    }

    @Test
    @DisplayName("测试获取文件信息 - 成功")
    void testGetFileInfo() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("size", 1024L);
        mockInfo.put("contentType", "text/plain");
        when(storageService.getFileInfo(anyString())).thenReturn(mockInfo);

        Map<String, Object> result = fileDownloadService.getFileInfo("test.txt");

        assertNotNull(result);
        assertEquals(1024L, result.get("size"));
        verify(storageService, times(1)).getFileInfo(anyString());
    }

    @Test
    @DisplayName("测试获取文件大小 - 成功")
    void testGetFileSize() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("size", 2048L);
        when(storageService.getFileInfo(anyString())).thenReturn(mockInfo);

        long result = fileDownloadService.getFileSize("test.txt");

        assertEquals(2048L, result);
    }

    @Test
    @DisplayName("测试获取文件内容类型 - 成功")
    void testGetContentType() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("contentType", "image/png");
        when(storageService.getFileInfo(anyString())).thenReturn(mockInfo);

        String result = fileDownloadService.getContentType("test.png");

        assertEquals("image/png", result);
    }
}
