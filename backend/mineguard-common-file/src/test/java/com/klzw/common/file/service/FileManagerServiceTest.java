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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 文件管理服务单元测试
 */
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
    @DisplayName("测试删除文件 - 成功")
    void testDeleteFile() {
        when(storageService.delete(anyString())).thenReturn(true);

        boolean result = fileManagerService.delete("test.txt");

        assertTrue(result);
        verify(storageService, times(1)).delete("test.txt");
    }

    @Test
    @DisplayName("测试批量删除文件 - 成功")
    void testDeleteBatch() {
        when(storageService.delete(anyString())).thenReturn(true);

        List<String> fileNames = Arrays.asList("test1.txt", "test2.txt", "test3.txt");
        int result = fileManagerService.deleteBatch(fileNames);

        assertEquals(3, result);
        verify(storageService, times(3)).delete(anyString());
    }

    @Test
    @DisplayName("测试重命名文件 - 成功")
    void testRename() throws Exception {
        byte[] fileContent = "Hello, World!".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.download(anyString())).thenReturn(inputStream);
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("new-test.txt");
        when(storageService.delete(anyString())).thenReturn(true);

        boolean result = fileManagerService.rename("test.txt", "new-test.txt");

        assertTrue(result);
        verify(storageService, times(1)).download("test.txt");
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
        verify(storageService, times(1)).delete("test.txt");
    }

    @Test
    @DisplayName("测试移动文件 - 成功")
    void testMove() throws Exception {
        byte[] fileContent = "Hello, World!".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(storageService.download(anyString())).thenReturn(inputStream);
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("new-path/test.txt");
        when(storageService.delete(anyString())).thenReturn(true);

        boolean result = fileManagerService.move("old-path/test.txt", "new-path/test.txt");

        assertTrue(result);
        verify(storageService, times(1)).download("old-path/test.txt");
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
        verify(storageService, times(1)).delete("old-path/test.txt");
    }
}
