package com.klzw.common.file.config;

import com.klzw.common.file.handler.BaiduOcrParserFactory;
import com.klzw.common.file.handler.TemplateOcrParser;
import com.klzw.common.file.impl.AliyunOssStorageServiceImpl;
import com.klzw.common.file.impl.FileDownloadServiceImpl;
import com.klzw.common.file.impl.FileManagerServiceImpl;
import com.klzw.common.file.impl.FileUploadServiceImpl;
import com.klzw.common.file.impl.OcrServiceImpl;
import com.klzw.common.file.properties.AliyunOssProperties;
import com.klzw.common.file.properties.BaiduAIProperties;
import com.klzw.common.file.properties.FileStorageProperties;
import com.klzw.common.file.service.StorageService;
import com.klzw.common.file.strategy.AliyunOssStorageStrategy;
import com.klzw.common.file.util.BaiduOcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "mineguard.file", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
        AliyunOssProperties.class, 
        BaiduAIProperties.class,
        FileStorageProperties.class
})
public class FileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StorageService aliyunOssStorageService(AliyunOssStorageStrategy aliyunOssStorageStrategy) {
        log.info("初始化 StorageService (Aliyun OSS)");
        return new AliyunOssStorageServiceImpl(aliyunOssStorageStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileUploadServiceImpl fileUploadService(StorageService storageService, FileStorageProperties fileStorageProperties) {
        log.info("初始化 FileUploadServiceImpl");
        return new FileUploadServiceImpl(storageService, fileStorageProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileManagerServiceImpl fileManagerService(StorageService storageService) {
        log.info("初始化 FileManagerServiceImpl");
        return new FileManagerServiceImpl(storageService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileDownloadServiceImpl fileDownloadService(StorageService storageService) {
        log.info("初始化 FileDownloadServiceImpl");
        return new FileDownloadServiceImpl(storageService);
    }

    @Bean
    @ConditionalOnMissingBean
    public BaiduOcrUtils baiduOcrUtils(BaiduAIProperties baiduAIProperties) {
        log.info("初始化 BaiduOcrUtils");
        return new BaiduOcrUtils(baiduAIProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateOcrParser templateOcrParser() {
        log.info("初始化 TemplateOcrParser");
        return new TemplateOcrParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public BaiduOcrParserFactory baiduOcrParserFactory(TemplateOcrParser templateOcrParser, BaiduAIProperties baiduAIProperties) {
        log.info("初始化 BaiduOcrParserFactory");
        return new BaiduOcrParserFactory(templateOcrParser, baiduAIProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mineguard.file.ocr.baidu", name = "enabled", havingValue = "true")
    public OcrServiceImpl ocrService(BaiduOcrUtils baiduOcrUtils, BaiduOcrParserFactory baiduOcrParserFactory) {
        log.info("初始化 OcrServiceImpl (Baidu OCR)");
        return new OcrServiceImpl(baiduOcrUtils, baiduOcrParserFactory);
    }
}
