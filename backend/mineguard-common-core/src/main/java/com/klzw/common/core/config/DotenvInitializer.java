package com.klzw.common.core.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Dotenv 初始化器
 * 在 Spring 容器初始化之前加载环境变量
 * 支持系统属性、Spring Environment 和系统环境变量三种方式
 */
@Slf4j
@Order(Integer.MIN_VALUE)
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 加载 .env 文件
        Dotenv dotenv = loadDotenv();
        
        if (dotenv.entries().isEmpty()) {
            log.warn("No .env file found or file is empty");
            return;
        }

        // 1. 设置系统属性 (System.setProperty)
        setSystemProperties(dotenv);
        
        // 2. 设置系统环境变量 (通过反射)
        setEnvironmentVariables(dotenv);
        
        // 3. 添加到 Spring Environment (最高优先级)
        addToSpringEnvironment(applicationContext, dotenv);
        
        log.info("Loaded {} environment variables from .env file", dotenv.entries().size());
        
        // 调试输出关键变量
        debugOutput();
    }

    /**
     * 加载 .env 文件，尝试多个路径
     */
    private Dotenv loadDotenv() {
        Dotenv dotenv;
        
        // 尝试路径1: 当前目录的上级目录
        dotenv = Dotenv.configure()
                .directory("../")
                .filename(".env")
                .ignoreIfMissing()
                .load();
        
        // 尝试路径2: 当前目录
        if (dotenv.entries().isEmpty()) {
            dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();
        }
        
        // 尝试路径3: 项目根目录 (上两级)
        if (dotenv.entries().isEmpty()) {
            dotenv = Dotenv.configure()
                    .directory("../../")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();
        }
        
        // 尝试路径4: 用户主目录
        if (dotenv.entries().isEmpty()) {
            dotenv = Dotenv.configure()
                    .directory(System.getProperty("user.home"))
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();
        }
        
        return dotenv;
    }

    /**
     * 设置系统属性
     */
    private void setSystemProperties(Dotenv dotenv) {
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        log.debug("System properties set successfully");
    }

    /**
     * 设置系统环境变量（通过反射，兼容 Java 8-24）
     */
    private void setEnvironmentVariables(Dotenv dotenv) {
        boolean success = false;
        
        // 方法1: 通过 ProcessEnvironment (Java 8/11)
        success = setEnvViaProcessEnvironment(dotenv);
        
        // 方法2: 通过 System.getenv() 的底层 Map (Java 9+)
        if (!success) {
            success = setEnvViaSystemGetenv(dotenv);
        }
        
        // 方法3: 通过 ProcessHandle (Java 9+)
        if (!success) {
            success = setEnvViaProcessHandle(dotenv);
        }
        
        if (success) {
            log.debug("Environment variables set successfully");
        } else {
            log.warn("Could not set environment variables via reflection, but system properties are set");
        }
    }

    /**
     * 通过 ProcessEnvironment 设置环境变量 (Java 8/11)
     */
    private boolean setEnvViaProcessEnvironment(Dotenv dotenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            
            // 获取 theEnvironment 字段
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Object envObj = theEnvironmentField.get(null);
            if (!(envObj instanceof Map)) {
                return false;
            }
            
            // 如果是 UnmodifiableMap，获取其底层 Map
            if (envObj.getClass().getName().contains("UnmodifiableMap")) {
                Field mapField = envObj.getClass().getDeclaredField("m");
                mapField.setAccessible(true);
                Object mapObj = mapField.get(envObj);
                if (!(mapObj instanceof Map)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                final Map<String, String> writableEnv = (Map<String, String>) mapObj;
                // 设置环境变量
                dotenv.entries().forEach(entry -> writableEnv.put(entry.getKey(), entry.getValue()));
            } else {
                @SuppressWarnings("unchecked")
                final Map<String, String> env = (Map<String, String>) envObj;
                // 设置环境变量
                dotenv.entries().forEach(entry -> env.put(entry.getKey(), entry.getValue()));
            }
            
            // 更新 theEnvironmentArray (可选)
            try {
                Field theEnvironmentArrayField = processEnvironmentClass.getDeclaredField("theEnvironmentArray");
                theEnvironmentArrayField.setAccessible(true);
                // 重新获取最新的环境变量
                Object currentEnvObj = theEnvironmentField.get(null);
                if (currentEnvObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> currentEnv = (Map<String, String>) currentEnvObj;
                    String[] envArray = new String[currentEnv.size()];
                    int i = 0;
                    for (Map.Entry<String, String> entry : currentEnv.entrySet()) {
                        envArray[i++] = entry.getKey() + "=" + entry.getValue();
                    }
                    theEnvironmentArrayField.set(null, envArray);
                }
            } catch (Exception e) {
                // 忽略，不是必须的
            }
            
            return true;
        } catch (Exception e) {
            log.debug("ProcessEnvironment approach failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过 System.getenv() 的底层 Map 设置环境变量 (Java 9+)
     */
    private boolean setEnvViaSystemGetenv(Dotenv dotenv) {
        try {
            // 获取 System.getenv() 返回的 Map 的底层字段
            Map<String, String> envMap = System.getenv();
            Field mapField = envMap.getClass().getDeclaredField("m");
            mapField.setAccessible(true);
            Object mapObj = mapField.get(envMap);
            if (!(mapObj instanceof Map)) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            final Map<String, String> writableEnv = (Map<String, String>) mapObj;
            
            // 设置环境变量
            dotenv.entries().forEach(entry -> writableEnv.put(entry.getKey(), entry.getValue()));
            
            return true;
        } catch (Exception e) {
            log.debug("System.getenv() approach failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过 ProcessHandle 设置环境变量 (Java 9+)
     */
    private boolean setEnvViaProcessHandle(Dotenv dotenv) {
        try {
            Class<?> processHandleImplClass = Class.forName("java.lang.ProcessHandleImpl");
            Field envField = processHandleImplClass.getDeclaredField("environ");
            envField.setAccessible(true);
            Object envObj = envField.get(null);
            if (envObj instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, String> env = (Map<String, String>) envObj;
                dotenv.entries().forEach(entry -> env.put(entry.getKey(), entry.getValue()));
                return true;
            }
        } catch (Exception e) {
            log.debug("ProcessHandle approach failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 添加到 Spring Environment (最高优先级)
     */
    private void addToSpringEnvironment(ConfigurableApplicationContext applicationContext, Dotenv dotenv) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // 方式1: 使用 MapPropertySource (适用于属性占位符)
        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> envMap.put(entry.getKey(), entry.getValue()));
        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));
        
        // 方式2: 使用 PropertiesPropertySource (适用于所有场景)
        Properties props = new Properties();
        dotenv.entries().forEach(entry -> props.setProperty(entry.getKey(), entry.getValue()));
        environment.getPropertySources().addFirst(new PropertiesPropertySource("dotenv-props", props));
        
        log.debug("Added {} environment variables to Spring Environment", dotenv.entries().size());
    }

    /**
     * 调试输出关键变量
     */
    private void debugOutput() {
        if (!log.isDebugEnabled()) {
            return;
        }
        
        String[] keys = {"NACOS_SERVER_ADDR", "NACOS_USERNAME", "NACOS_PASSWORD", 
                         "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD"};
        
        log.debug("====== 环境变量诊断 ======");
        for (String key : keys) {
            log.debug("{} -> System.getenv(): [{}], System.getProperty(): [{}]", 
                key,
                System.getenv(key),
                System.getProperty(key)
            );
        }
    }
}