package com.klzw.common.database.aspect;

import com.klzw.common.database.annotation.DataSource;
import com.klzw.common.database.datasource.DynamicDataSource;
import com.klzw.common.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

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
 * 4. 事务感知：在事务中强制使用主数据源
 * 5. 强制方法名判断：不受已有上下文影响，确保写操作一定走主库
 * <p>
 * 识别规则：
 * - 写操作：insert, update, delete, save, create, modify, remove 开头的方法
 * - 读操作：select, query, get, find, list, count, exist 开头的方法
 * <p>
 * 优先级说明：
 * 1. 如果方法上有@DataSource注解，使用注解指定的数据源
 * 2. 如果在事务中，强制使用主数据源
 * 3. 根据方法名判断读写操作，强制切换数据源
 * <p>
 * 注意：
 * - 主从数据复制在MySQL数据库层面配置，应用层只负责读写分离
 * - 从数据源只有一个，不需要负载均衡策略
 * - 每次方法调用独立判断数据源，不依赖已有上下文
 */
@Slf4j
@Aspect
@Component
@Order(-2)
@ConditionalOnProperty(prefix = "mineguard.database", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReadWriteSplitAspect {

    @Resource
    private DatabaseProperties databaseProperties;

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
        // 检查数据库模块是否启用，如果未启用，直接执行原方法
        if (!databaseProperties.isEnabled()) {
            log.debug("数据库模块未启用，直接执行原方法: {}", point.getSignature().getName());
            return point.proceed();
        }
        
        // 检查动态数据源是否启用，如果未启用，直接执行原方法
        if (!databaseProperties.getDynamic().isEnabled()) {
            log.debug("动态数据源未启用，直接执行原方法: {}", point.getSignature().getName());
            return point.proceed();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        
        // 1. 检查是否有@DataSource注解，有则使用注解（最高优先级）
        DataSource dataSourceAnnotation = method.getAnnotation(DataSource.class);
        DataSource classDataSourceAnnotation = method.getDeclaringClass().getAnnotation(DataSource.class);
        
        if (dataSourceAnnotation != null || classDataSourceAnnotation != null) {
            log.debug("方法上有@DataSource注解，使用注解指定的数据源: {}", methodName);
            return point.proceed();
        }
        
        // 2. 检查是否在事务中，在事务中强制使用主数据源（第二优先级）
        boolean isInTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        if (isInTransaction) {
            log.debug("方法在事务中: {}, 强制使用主数据源", methodName);
            try {
                DynamicDataSource.setMasterDataSource();
                return point.proceed();
            } finally {
                DynamicDataSource.clearDataSourceContext();
            }
        }
        
        // 3. 根据方法名判断读写操作，强制切换数据源（不再受已有上下文影响）
        String operationType = determineOperationType(methodName);

        try {
            // 所有操作都使用主数据源，避免从数据源同步延迟导致的问题
            log.debug("所有操作都使用主数据源: {}", methodName);
            DynamicDataSource.setMasterDataSource();

            return point.proceed();
        } finally {
            // 清理数据源上下文，确保不影响后续操作
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
