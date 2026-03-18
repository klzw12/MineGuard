package com.klzw.service.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mineguard.user")
public class AdminInitProperties {
    private boolean initadmin = false;
    private String adminUsername = "admin";
    private String adminPassword = "admin123";
    private String adminRealName = "系统管理员";
    private String adminPhone = "13800138000";
    private String adminEmail = "admin@mineguard.com";
}
