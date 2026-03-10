package com.klzw.common.file.config;

import com.klzw.common.file.properties.AliyunOssProperties;
import com.klzw.common.file.strategy.AliyunOssStorageStrategy;
import com.klzw.common.file.strategy.StorageStrategy;
import com.aliyun.oss.OSS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(prefix = "mineguard.file.storage.aliyun-oss", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AliyunOssProperties.class)
public class AliyunOssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StorageStrategy.class)
    public StorageStrategy aliyunOssStorageStrategy(AliyunOssProperties aliyunOssProperties) {
        log.info("初始化 AliyunOssStorageStrategy");
        return new AliyunOssStorageStrategy(aliyunOssProperties);
    }
}
