package com.klzw.service.user;

import org.junit.jupiter.api.Test;

public class Jackson3Test {

    @Test
    public void testJackson3Features() {
        System.out.println("=== 测试 Jackson 3.x 特性 ===");
        
        try {
            // 测试 SerializationFeature
            Class<?> serializationFeatureClass = Class.forName("tools.jackson.databind.SerializationFeature");
            System.out.println("SerializationFeature 类加载成功");
            
            // 测试 DateTimeFeature
            Class<?> dateTimeFeatureClass = Class.forName("tools.jackson.databind.cfg.DateTimeFeature");
            System.out.println("DateTimeFeature 类加载成功");
            
            // 打印所有 DateTimeFeature 常量
            System.out.println("\nDateTimeFeature 常量:");
            Object[] dateTimeConstants = dateTimeFeatureClass.getEnumConstants();
            if (dateTimeConstants != null) {
                for (Object constant : dateTimeConstants) {
                    System.out.println("  - " + constant);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
