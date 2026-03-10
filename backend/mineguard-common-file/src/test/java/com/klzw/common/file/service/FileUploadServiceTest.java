package com.klzw.common.file.service;

import com.klzw.common.file.enums.FileBusinessTypeEnum;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.common.file.properties.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传服务单元测试")
@Tag("unit")
class FileUploadServiceTest {

    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    @Mock
    private StorageService storageService;

    @Mock
    private FileStorageProperties fileStorageProperties;

    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile("test.png", "test.png", "image/png", "test image content".getBytes());
        when(fileStorageProperties.getMaxFileSize()).thenReturn(10 * 1024 * 1024L);
        when(fileStorageProperties.getAllowedExtensions()).thenReturn("jpg,jpeg,png,gif,bmp,pdf,doc,docx,xls,xlsx");
    }

    @Test
    @DisplayName("测试上传文件 - 成功")
    void testUploadFile() throws IOException {
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("/test/test.png");

        String result = fileUploadService.upload(testFile, FileBusinessTypeEnum.USER_AVATAR, "test-user");

        assertNotNull(result);
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
    }

    @Test
    @DisplayName("测试上传文件（指定业务类型）- 成功")
    void testUploadFileWithBusinessType() throws IOException {
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("/test/test.png");

        String result = fileUploadService.upload(testFile, FileBusinessTypeEnum.USER_AVATAR);

        assertNotNull(result);
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
    }

    @Test
    @DisplayName("测试上传文件（仅文件）- 成功")
    void testUploadFileOnly() throws IOException {
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("/test/test.png");

        String result = fileUploadService.upload(testFile);

        assertNotNull(result);
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
    }

    @Test
    @DisplayName("测试上传文件（整型业务类型）- 成功")
    void testUploadFileWithIntBusinessType() throws IOException {
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("/test/test.png");

        String result = fileUploadService.upload(testFile, 1, "test-user");

        assertNotNull(result);
        verify(storageService, times(1)).upload(any(InputStream.class), anyString(), anyString());
    }
}
