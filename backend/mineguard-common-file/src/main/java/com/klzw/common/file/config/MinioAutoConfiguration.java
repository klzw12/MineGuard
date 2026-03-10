package com.klzw.common.file.config;

import com.klzw.common.file.properties.MinioProperties;
import com.klzw.common.file.strategy.MinioStorageStrategy;
import com.klzw.common.file.strategy.StorageStrategy;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "mineguard.file.storage.minio", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StorageStrategy.class)
    public StorageStrategy minioStorageStrategy(MinioProperties minioProperties) {
        log.info("初始化 MinioStorageStrategy");
        return new MinioStorageStrategy(minioProperties);
    }
}
