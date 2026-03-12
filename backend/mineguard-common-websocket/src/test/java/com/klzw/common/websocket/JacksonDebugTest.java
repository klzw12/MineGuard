package com.klzw.common.websocket;

import org.junit.jupiter.api.Test;
import java.security.CodeSource;

public class JacksonDebugTest {

    @Test
    public void testJacksonVersion() {
        System.out.println("\n=== Jackson 运行时诊断 ===");

        try {
            Class<?> clazz = Class.forName("tools.jackson.databind.SerializationFeature");
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            System.out.println("SerializationFeature loaded from: " + (codeSource != null ? codeSource.getLocation() : "unknown"));

            Package pkg = clazz.getPackage();
            System.out.println("Package: " + pkg.getName());
            System.out.println("Implementation version: " + pkg.getImplementationVersion());
            System.out.println("Specification version: " + pkg.getSpecificationVersion());

            System.out.println("\n所有可用的枚举常量:");
            Object[] constants = clazz.getEnumConstants();
            if (constants != null) {
                for (Object constant : constants) {
                    System.out.println("  - " + constant);
                }
            } else {
                System.out.println("  无法获取枚举常量");
            }

            System.out.println("\n检查 WRITE_DATES_AS_TIMESTAMPS:");
            try {
                Enum.valueOf((Class<Enum>) clazz, "WRITE_DATES_AS_TIMESTAMPS");
                System.out.println("✅ WRITE_DATES_AS_TIMESTAMPS 存在");
            } catch (IllegalArgumentException e) {
                System.out.println("❌ WRITE_DATES_AS_TIMESTAMPS 不存在");
                System.out.println("   原因: " + e.getMessage());
            }

            System.out.println("\n检查 DateTimeFeature:");
            try {
                Class<?> dateTimeFeatureClass = Class.forName("tools.jackson.databind.cfg.DateTimeFeature");
                System.out.println("✅ DateTimeFeature 存在");
                Object[] dateTimeConstants = dateTimeFeatureClass.getEnumConstants();
                if (dateTimeConstants != null) {
                    System.out.println("  可用的 DateTimeFeature 常量:");
                    for (Object constant : dateTimeConstants) {
                        System.out.println("    - " + constant);
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("❌ DateTimeFeature 不存在");
            }

            System.out.println("\n类加载器信息:");
            ClassLoader cl = clazz.getClassLoader();
            System.out.println("ClassLoader: " + cl);
            System.out.println("ClassLoader parent: " + cl.getParent());

            System.out.println("\n检查 fastxml 类:");
            try {
                Class<?> fastxmlClass = Class.forName("com.fasterxml.jackson.databind.SerializationFeature");
                System.out.println("fasterxml SerializationFeature 也存在!");
                CodeSource fastxmlSource = fastxmlClass.getProtectionDomain().getCodeSource();
                System.out.println("  loaded from: " + fastxmlSource.getLocation());
            } catch (ClassNotFoundException e) {
                System.out.println("fasterxml SerializationFeature 不存在");
            }
        } catch (Exception e) {
            System.out.println("诊断过程中出错:");
            e.printStackTrace();
        }
    }

    @Test
    public void testObjectMapper() {
        System.out.println("\n=== ObjectMapper 诊断 ===");

        try {
            Class<?> objectMapperClass = Class.forName("tools.jackson.databind.ObjectMapper");
            Object mapper = objectMapperClass.getDeclaredConstructor().newInstance();

            System.out.println("ObjectMapper class: " + mapper.getClass().getName());
            CodeSource codeSource = mapper.getClass().getProtectionDomain().getCodeSource();
            System.out.println("ObjectMapper loaded from: " + (codeSource != null ? codeSource.getLocation() : "unknown"));
        } catch (Exception e) {
            System.out.println("ObjectMapper 诊断失败:");
            e.printStackTrace();
        }
    }
}
