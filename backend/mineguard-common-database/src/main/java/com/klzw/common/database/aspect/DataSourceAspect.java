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

/**
 * 数据源切换切面
 * <p>
 * 主要功能：
 * 1. 拦截带有@DataSource注解的方法
 * 2. 在方法执行前切换数据源
 * 3. 在方法执行后恢复数据源
 */
@Slf4j
@Aspect
@Component
@Order(-1)
public class DataSourceAspect {

    @Pointcut("@annotation(com.klzw.common.database.annotation.DataSource)")
    public void dataSourcePointCut() {
    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (dataSource == null) {
            dataSource = method.getDeclaringClass().getAnnotation(DataSource.class);
        }
        
        if (dataSource == null) {
            return point.proceed();
        }
        
        String dataSourceName = dataSource.value();
        
        try {
            log.debug("切换数据源到: {}", dataSourceName);
            
            if (DynamicDataSource.MASTER.equals(dataSourceName)) {
                DynamicDataSource.setMasterDataSource();
            } else if (DynamicDataSource.SLAVE.equals(dataSourceName)) {
                DynamicDataSource.setSlaveDataSource();
            } else {
                log.warn("未知的数据源类型: {}, 使用默认主数据源", dataSourceName);
                DynamicDataSource.setMasterDataSource();
            }
            
            return point.proceed();
        } finally {
            log.debug("恢复数据源到默认值");
            DynamicDataSource.clearDataSourceContext();
        }
    }
}
