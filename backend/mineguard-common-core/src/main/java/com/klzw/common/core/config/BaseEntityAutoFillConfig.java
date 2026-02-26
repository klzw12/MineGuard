package com.klzw.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BaseEntityAutoFillConfig {
    
    public static final int INITIAL_VERSION = 1;


    public static void fillVersion(Object entity) {
        if (entity == null) {
            return;
        }
        
        try {
            java.lang.reflect.Field versionField = entity.getClass().getDeclaredField("version");
            versionField.setAccessible(true);
            if (versionField.get(entity) == null) {
                versionField.set(entity, INITIAL_VERSION);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.debug("实体 {} 不存在版本号字段或访问异常: {}", entity.getClass().getName(), e.getMessage());
        }
    }

    public static void incrementVersion(Object entity) {
        if (entity == null) {
            return;
        }
        
        try {
            java.lang.reflect.Field versionField = entity.getClass().getDeclaredField("version");
            versionField.setAccessible(true);
            Integer version = (Integer) versionField.get(entity);
            if (version != null) {
                versionField.set(entity, version + 1);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.debug("实体 {} 不存在版本号字段或访问异常: {}", entity.getClass().getName(), e.getMessage());
        }
    }
}