package com.klzw.common.file.service;

import com.klzw.common.file.impl.FileDownloadServiceImpl;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 文件下载服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件下载服务单元测试")
@Tag("unit")
class FileDownloadServiceTest {

    @InjectMocks
    private FileDownloadServiceImpl fileDownloadService;

    @Mock
    private StorageService storageService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("测试下载文件 - 成功")
    void testDownloadFile() throws Exception {
        byte[] fileContent = "Hello, World!".getBytes();
        InputStream expectedInputStream = new ByteArrayInputStream(fileContent);

        when(storageService.download(anyString())).thenReturn(expectedInputStream);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            }
        });

        fileDownloadService.download("test.txt", response);

        verify(storageService, times(1)).download("test.txt");
    }

    @Test
    @DisplayName("测试下载文件（指定新文件名）- 成功")
    void testDownloadFileWithNewFileName() throws Exception {
        byte[] fileContent = "Hello, World!".getBytes();
        InputStream expectedInputStream = new ByteArrayInputStream(fileContent);

        when(storageService.download(anyString())).thenReturn(expectedInputStream);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            }
        });

        fileDownloadService.download("test.txt", response, "new-test.txt");

        verify(storageService, times(1)).download("test.txt");
    }
}
