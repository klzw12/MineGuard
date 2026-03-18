package com.klzw.service.user.config;

import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.repository.MessageHistoryRepository;
import com.klzw.service.user.service.sms.SmsService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@TestConfiguration
public class TestMockConfig {

    @Bean
    @Primary
    public StorageService storageService() {
        return new StorageService() {
            @Override
            public String upload(InputStream inputStream, String fileName, String contentType) {
                return "http://test-minio.example.com/test-bucket/" + fileName;
            }

            @Override
            public String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType) {
                return "http://test-minio.example.com/test-bucket/" + module + "/" + folder + "/" + originalFileName;
            }

            @Override
            public InputStream download(String fileName) {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public boolean delete(String fileName) {
                return true;
            }

            @Override
            public String getUrl(String fileName, long expire) {
                return "http://test-minio.example.com/test-bucket/" + fileName;
            }

            @Override
            public Map<String, Object> getFileInfo(String fileName) {
                Map<String, Object> info = new HashMap<>();
                info.put("fileName", fileName);
                info.put("size", 0);
                return info;
            }

            @Override
            public boolean isHealthy() {
                return true;
            }
        };
    }

    @Bean
    @Primary
    public OcrService ocrService() {
        return new OcrService() {
            @Override
            public String recognizeIdCard(MultipartFile file) {
                return "{\"name\":\"测试用户\",\"idCard\":\"110101199001011234\"}";
            }

            @Override
            public String recognizeIdCardBack(MultipartFile file) {
                return "{}";
            }

            @Override
            public String recognizeDrivingLicense(MultipartFile file) {
                return "{\"name\":\"测试用户\",\"licenseType\":\"C1\"}";
            }

            @Override
            public String recognizeVehicleLicense(MultipartFile file) {
                return "{}";
            }

            @Override
            public String recognizeVehicleLicenseBack(MultipartFile file) {
                return "{}";
            }

            @Override
            public String recognizeLicensePlate(MultipartFile file) {
                return "{\"plateNumber\":\"京A12345\"}";
            }

            @Override
            public String recognizeEmergencyCert(MultipartFile file) {
                return "{\"name\":\"测试用户\",\"certNumber\":\"AQ123456789\"}";
            }

            @Override
            public String recognizeRepairCert(MultipartFile file) {
                return "{\"name\":\"测试用户\",\"certNumber\":\"WX123456789\"}";
            }

            @Override
            public Map<String, String> parseIdCard(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("name", "测试用户");
                result.put("idNumber", "110101199001011234");
                result.put("gender", "男");
                result.put("nation", "汉");
                result.put("birth", "1990-01-01");
                result.put("address", "北京市东城区测试路123号");
                return result;
            }

            @Override
            public Map<String, String> parseDrivingLicense(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("name", "测试用户");
                result.put("准驾车型", "C1");
                result.put("gender", "男");
                result.put("address", "北京市东城区测试路123号");
                result.put("birth", "1990-01-01");
                result.put("firstIssueDate", "2015-01-01");
                result.put("validPeriod", "2021-01-01至2027-01-01");
                result.put("licenseNumber", "110101199001011234");
                return result;
            }

            @Override
            public Map<String, String> parseRepairCert(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("name", "测试用户");
                result.put("certificateNumber", "WX123456789");
                result.put("level", "高级");
                result.put("category", "电气维修");
                result.put("validUntil", "2030-12-31");
                result.put("issueDate", "2020-01-01");
                return result;
            }

            @Override
            public Map<String, String> parseEmergencyCert(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("name", "测试用户");
                result.put("certificateNumber", "AQ123456789");
                result.put("trainingItem", "应急救援");
                result.put("validPeriod", "2030-12-31");
                return result;
            }

            @Override
            public Map<String, String> parseLicensePlate(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("plateNumber", "京A12345");
                return result;
            }

            @Override
            public Map<String, String> parseVehicleLicenseFront(String ocrResult) {
                return new HashMap<>();
            }

            @Override
            public Map<String, String> parseVehicleLicenseBack(String ocrResult) {
                return new HashMap<>();
            }

            @Override
            public Map<String, String> parseGeneral(String ocrResult) {
                Map<String, String> result = new HashMap<>();
                result.put("text", ocrResult);
                return result;
            }

            @Override
            public Map<String, String> parse(String ocrResult, String certType) {
                return parseGeneral(ocrResult);
            }
        };
    }

    @Bean
    @Primary
    public SmsService smsService() {
        return new SmsService() {
            @Override
            public boolean sendSmsCode(String phone) {
                return true;
            }

            @Override
            public boolean sendSmsVerificationCode(String phone, String templateId, String[] params) {
                return true;
            }

            @Override
            public boolean sendLoginRegisterCode(String phone) {
                return true;
            }

            @Override
            public boolean sendUpdatePhoneCode(String phone) {
                return true;
            }

            @Override
            public boolean sendResetPasswordCode(String phone) {
                return true;
            }

            @Override
            public boolean sendBindPhoneCode(String phone) {
                return true;
            }

            @Override
            public boolean sendVerifyPhoneCode(String phone) {
                return true;
            }

            @Override
            public boolean verifySmsCode(String phone, String code) {
                return true;
            }
        };
    }

    @Bean
    @Primary
    public RedisCacheService redisCacheService() {
        return new RedisCacheService(null) {
            private final Map<String, Object> cache = new HashMap<>();

            @Override
            public void set(String key, Object value, long expire, TimeUnit timeUnit) {
                cache.put(key, value);
            }

            @Override
            public void set(String key, Object value) {
                cache.put(key, value);
            }

            @Override
            public void setBatch(Map<String, Object> map, long expire, TimeUnit timeUnit) {
                cache.putAll(map);
            }

            @Override
            public void setBatch(Map<String, Object> map) {
                cache.putAll(map);
            }

            @Override
            public <T> T get(String key) {
                return (T) cache.get(key);
            }

            @Override
            public <T> Map<String, T> getBatch(java.util.Collection<String> keys) {
                Map<String, T> result = new HashMap<>();
                for (String key : keys) {
                    T value = (T) cache.get(key);
                    if (value != null) {
                        result.put(key, value);
                    }
                }
                return result;
            }

            @Override
            public void delete(String key) {
                cache.remove(key);
            }

            @Override
            public Long deleteBatch(java.util.Collection<String> keys) {
                long count = 0;
                for (String key : keys) {
                    if (cache.remove(key) != null) {
                        count++;
                    }
                }
                return count;
            }

            @Override
            public Boolean exists(String key) {
                return cache.containsKey(key);
            }

            @Override
            public Long increment(String key) {
                Object value = cache.get(key);
                long current = (value != null) ? Long.parseLong(value.toString()) : 0;
                long result = current + 1;
                cache.put(key, result);
                return result;
            }

            @Override
            public Long increment(String key, long delta) {
                Object value = cache.get(key);
                long current = (value != null) ? Long.parseLong(value.toString()) : 0;
                long result = current + delta;
                cache.put(key, result);
                return result;
            }

            @Override
            public Long decrement(String key) {
                Object value = cache.get(key);
                long current = (value != null) ? Long.parseLong(value.toString()) : 0;
                long result = current - 1;
                cache.put(key, result);
                return result;
            }
        };
    }

    @Bean
    @Primary
    public MessageHistoryRepository messageHistoryRepository() {
        return Mockito.mock(MessageHistoryRepository.class);
    }
}
