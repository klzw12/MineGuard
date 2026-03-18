package com.klzw.service.user.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mineguard.sms.aliyun.enabled", havingValue = "true")
public class AliyunSmsConfig {

    private final AliyunSmsProperties aliyunSmsProperties;

    @Bean
    public Client aliyunSmsClient() {
        log.info("[阿里云Dypnsapi] 初始化客户端...");
        
        if (aliyunSmsProperties.getAccessKeyId() == null || aliyunSmsProperties.getAccessKeyId().isEmpty()) {
            log.error("[阿里云Dypnsapi] 配置错误：AccessKeyId不能为空");
            throw new IllegalArgumentException("阿里云短信服务配置错误：AccessKeyId不能为空");
        }
        
        if (aliyunSmsProperties.getAccessKeySecret() == null || aliyunSmsProperties.getAccessKeySecret().isEmpty()) {
            log.error("[阿里云Dypnsapi] 配置错误：AccessKeySecret不能为空");
            throw new IllegalArgumentException("阿里云短信服务配置错误：AccessKeySecret不能为空");
        }
        
        try {
            Config config = new Config()
                    .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                    .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret())
                    .setEndpoint(aliyunSmsProperties.getEndpoint());
            
            Client client = new Client(config);
            log.info("[阿里云Dypnsapi] 客户端初始化完成");
            return client;
        } catch (Exception e) {
            log.error("[阿里云Dypnsapi] 客户端初始化失败：{}", e.getMessage(), e);
            throw new RuntimeException("阿里云短信服务初始化失败：" + e.getMessage(), e);
        }
    }
}
