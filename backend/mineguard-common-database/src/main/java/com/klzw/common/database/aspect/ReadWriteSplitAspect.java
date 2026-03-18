package com.klzw.common.database.aspect;

import com.klzw.common.database.annotation.DataSource;
import com.klzw.common.database.datasource.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 读写分离切面
 * <p>
 * 主要功能：
 * 1. 根据方法名自动判断读写操作
 * 2. 读操作使用从数据源，写操作使用主数据源
 * 3. 与@DataSource注解兼容，@DataSource注解优先级更高
 * <p>
 * 识别规则：
 * - 写操作：insert, update, delete, save, create, modify, remove 开头的方法
 * - 读操作：select, query, get, find, list, count, exist 开头的方法
 * <p>
 * 优先级说明：
 * - 如果方法上有@DataSource注解，使用注解指定的数据源，不进行自动读写分离
 * - 如果方法上没有@DataSource注解，根据方法名自动判断读写操作
 * <p>
 * 注意：
 * - 主从数据复制在MySQL数据库层面配置，应用层只负责读写分离
 * - 从数据源只有一个，不需要负载均衡策略
 */
@Slf4j
@Aspect
@Component
@Order(-2)
public class ReadWriteSplitAspect {

    /**
     * 写操作前缀列表
     */
    private static final List<String> WRITE_PREFIXES = Arrays.asList(
            "insert", "update", "delete", "save", "create", "modify", "remove"
    );

    /**
     * 读操作前缀列表
     */
    private static final List<String> READ_PREFIXES = Arrays.asList(
            "select", "query", "get", "find", "list", "count", "exist"
    );

    @Pointcut("execution(* com.klzw..service..*.*(..))")
    public void servicePointcut() {
    }

    @Around("servicePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        DataSource dataSourceAnnotation = method.getAnnotation(DataSource.class);
        DataSource classDataSourceAnnotation = method.getDeclaringClass().getAnnotation(DataSource.class);
        
        if (dataSourceAnnotation != null || classDataSourceAnnotation != null) {
            log.debug("方法上有@DataSource注解，使用注解指定的数据源: {}", method.getName());
            return point.proceed();
        }
        
        String currentContext = DynamicDataSource.getCurrentDataSourceContext();
        if (currentContext != null) {
            log.debug("已有数据源上下文: {}, 保持当前数据源, 方法: {}", currentContext, method.getName());
            return point.proceed();
        }
        
        String methodName = point.getSignature().getName();
        String operationType = determineOperationType(methodName);

        try {
            if ("WRITE".equals(operationType)) {
                log.debug("检测到写操作: {}, 使用主数据源", methodName);
                DynamicDataSource.setMasterDataSource();
            } else if ("READ".equals(operationType)) {
                log.debug("检测到读操作: {}, 使用从数据源", methodName);
                DynamicDataSource.setSlaveDataSource();
            } else {
                log.debug("无法判断操作类型: {}, 使用默认主数据源", methodName);
                DynamicDataSource.setMasterDataSource();
            }

            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSourceContext();
        }
    }

    /**
     * 判断操作类型
     * @param methodName 方法名
     * @return 操作类型：READ/WRITE/UNKNOWN
     */
    private String determineOperationType(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return "UNKNOWN";
        }

        String lowerMethodName = methodName.toLowerCase();

        for (String prefix : WRITE_PREFIXES) {
            if (lowerMethodName.startsWith(prefix)) {
                return "WRITE";
            }
        }

        for (String prefix : READ_PREFIXES) {
            if (lowerMethodName.startsWith(prefix)) {
                return "READ";
            }
        }

        return "UNKNOWN";
    }
}
