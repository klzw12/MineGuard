package com.klzw.service.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "mineguard.sms.aliyun")
public class AliyunSmsProperties {
    
    private boolean enabled = false;
    
    private String accessKeyId;
    
    private String accessKeySecret;
    
    private String endpoint = "dypnsapi.aliyuncs.com";
    
    private String signName = "速通互联验证码";
    
    private Map<String, String> templates = new HashMap<>();
    
    public String getTemplateCode(String scene) {
        if (templates.containsKey(scene)) {
            return templates.get(scene);
        }
        return templates.getOrDefault("REGISTER", "SMS_100001");
    }
}
