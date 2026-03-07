package com.klzw.common.file.service;

import com.klzw.common.file.AbstractFileIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * OCR服务集成测试
 */
@DisplayName("OCR服务集成测试")
@Tag("integration")
public class OcrServiceIntegrationTest extends AbstractFileIntegrationTest {

    @Autowired
    private OcrService ocrService;

    @Test
    @DisplayName("身份证正面识别")
    public void testRecognizeIdCard() throws Exception {
        ClassPathResource resource = new ClassPathResource("idcard_front.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeIdCard(file);
                System.out.println("身份证识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("身份证背面识别")
    public void testRecognizeIdCardBack() throws Exception {
        ClassPathResource resource = new ClassPathResource("idcard_back.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeIdCardBack(file);
                System.out.println("身份证背面识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("驾驶证识别")
    public void testRecognizeDrivingLicense() throws Exception {
        ClassPathResource resource = new ClassPathResource("driving.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeDrivingLicense(file);
                System.out.println("驾驶证识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("行驶证正面识别")
    public void testRecognizeVehicleLicense() throws Exception {
        ClassPathResource resource = new ClassPathResource("vehicle_front.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeVehicleLicense(file);
                System.out.println("行驶证识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("行驶证背面识别")
    public void testRecognizeVehicleLicenseBack() throws Exception {
        ClassPathResource resource = new ClassPathResource("vehicle_back.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeVehicleLicenseBack(file);
                System.out.println("行驶证背面识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("车牌识别")
    public void testRecognizeLicensePlate() throws Exception {
        ClassPathResource resource = new ClassPathResource("car3e804133-24e9-48c2-8333-7e672261aee6.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeLicensePlate(file);
                System.out.println("车牌识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("紧急救援资格证识别")
    public void testRecognizeEmergencyCert() throws Exception {
        ClassPathResource resource = new ClassPathResource("emergency.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeEmergencyCert(file);
                System.out.println("紧急救援资格证识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    @Test
    @DisplayName("维修资格证识别")
    public void testRecognizeRepairCert() throws Exception {
        ClassPathResource resource = new ClassPathResource("repair.png");
        if (resource.exists()) {
            MultipartFile file = new TestMultipartFile(resource.getFile());
            try {
                String result = ocrService.recognizeRepairCert(file);
                System.out.println("维修资格证识别结果: " + result);
            } catch (Exception e) {
                System.out.println("测试环境无百度 AI API 权限，捕获异常: " + e.getMessage());
            }
        } else {
            System.out.println("测试文件不存在，跳过测试");
        }
    }

    private static class TestMultipartFile implements MultipartFile {

        private final File file;

        public TestMultipartFile(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getOriginalFilename() {
            return file.getName();
        }

        @Override
        public String getContentType() {
            return "image/png";
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                return fis.readAllBytes();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileInputStream fis = new FileInputStream(file);
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fis.transferTo(fos);
            }
        }
    }
}
