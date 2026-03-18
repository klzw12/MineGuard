package com.klzw.service.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mineguard.sms.aliyun")
public class AliyunSmsProperties {
    
    private boolean enabled = false;
    
    private String accessKeyId;
    
    private String accessKeySecret;
    
    private String endpoint = "dypnsapi.aliyuncs.com";
    
    private String signName = "速通互联验证码";
}
