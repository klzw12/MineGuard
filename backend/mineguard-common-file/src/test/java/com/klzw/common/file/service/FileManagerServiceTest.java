package com.klzw.common.file.service;

import com.klzw.common.file.impl.FileManagerServiceImpl;
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
@DisplayName("文件管理服务单元测试")
@Tag("unit")
class FileManagerServiceTest {

    @InjectMocks
    private FileManagerServiceImpl fileManagerService;

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

        InputStream result = fileManagerService.download("test.txt");

        assertNotNull(result);
        verify(storageService, times(1)).download(anyString());
    }

    @Test
    @DisplayName("测试删除文件 - 成功")
    void testDelete() {
        when(storageService.delete(anyString())).thenReturn(true);

        boolean result = fileManagerService.delete("test.txt");

        assertTrue(result);
        verify(storageService, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("测试获取文件URL - 成功")
    void testGetUrl() {
        when(storageService.getUrl(anyString(), anyLong())).thenReturn("http://test.url/file");

        String result = fileManagerService.getUrl("test.txt", 3600);

        assertNotNull(result);
        verify(storageService, times(1)).getUrl(anyString(), anyLong());
    }

    @Test
    @DisplayName("测试获取文件信息 - 成功")
    void testGetFileInfo() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("size", 1024L);
        mockInfo.put("contentType", "text/plain");
        when(storageService.getFileInfo(anyString())).thenReturn(mockInfo);

        Map<String, Object> result = fileManagerService.getFileInfo("test.txt");

        assertNotNull(result);
        assertEquals(1024L, result.get("size"));
        verify(storageService, times(1)).getFileInfo(anyString());
    }

    @Test
    @DisplayName("测试检查文件是否存在 - 存在")
    void testExists_True() {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("size", 1024L);
        when(storageService.getFileInfo(anyString())).thenReturn(mockInfo);

        boolean result = fileManagerService.exists("test.txt");

        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查文件是否存在 - 不存在")
    void testExists_False() {
        when(storageService.getFileInfo(anyString())).thenThrow(new RuntimeException("File not found"));

        boolean result = fileManagerService.exists("test.txt");

        assertFalse(result);
    }
}
