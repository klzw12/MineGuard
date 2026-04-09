package com.klzw.common.database.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态数据源类
 * 用于管理主从数据源的切换
 * 
 * 容灾功能：
 * 1. 从库故障时自动切换到主库
 * 2. 定期检测从库恢复状态
 * 3. 从库恢复后自动切换回从库
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATASOURCE_CONTEXT = new ThreadLocal<>();

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    private static final AtomicBoolean slaveAvailable = new AtomicBoolean(true);
    private static final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final long CHECK_INTERVAL = 30000;
    
    private DataSource masterDataSource;
    private DataSource slaveDataSource;

    public DynamicDataSource(DataSource defaultDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
        
        this.masterDataSource = (DataSource) targetDataSources.get(MASTER);
        this.slaveDataSource = (DataSource) targetDataSources.get(SLAVE);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String currentKey = DATASOURCE_CONTEXT.get();
        
        if (SLAVE.equals(currentKey)) {
            if (!isSlaveAvailable()) {
                log.warn("从库不可用，自动切换到主库");
                return MASTER;
            }
        }
        
        return currentKey;
    }
    
    @Override
    protected DataSource determineTargetDataSource() {
        String lookupKey = (String) determineCurrentLookupKey();
        
        if (SLAVE.equals(lookupKey) && !isSlaveAvailable()) {
            log.warn("从库不可用，返回主数据源");
            return masterDataSource;
        }
        
        return super.determineTargetDataSource();
    }

    /**
     * 检查从库是否可用
     */
    private boolean isSlaveAvailable() {
        long now = System.currentTimeMillis();
        long lastCheck = lastCheckTime.get();
        
        if (now - lastCheck > CHECK_INTERVAL) {
            if (lastCheckTime.compareAndSet(lastCheck, now)) {
                checkSlaveHealth();
            }
        }
        
        return slaveAvailable.get();
    }
    
    /**
     * 检查从库健康状态
     */
    private void checkSlaveHealth() {
        if (slaveDataSource == null) {
            slaveAvailable.set(false);
            return;
        }
        
        try (Connection conn = slaveDataSource.getConnection()) {
            if (conn.isValid(3)) {
                if (!slaveAvailable.get()) {
                    log.info("从库已恢复，重新启用从库读取");
                }
                slaveAvailable.set(true);
            } else {
                slaveAvailable.set(false);
                log.warn("从库连接无效，标记为不可用");
            }
        } catch (SQLException e) {
            slaveAvailable.set(false);
            log.warn("从库健康检查失败: {}", e.getMessage());
        }
    }
    
    /**
     * 手动标记从库不可用
     */
    public static void markSlaveUnavailable() {
        slaveAvailable.set(false);
        log.warn("从库已被手动标记为不可用");
    }
    
    /**
     * 手动标记从库可用
     */
    public static void markSlaveAvailable() {
        slaveAvailable.set(true);
        log.info("从库已被手动标记为可用");
    }
    
    /**
     * 获取从库状态
     */
    public static boolean getSlaveStatus() {
        return slaveAvailable.get();
    }

    public static void setMasterDataSource() {
        DATASOURCE_CONTEXT.set(MASTER);
    }

    public static void setSlaveDataSource() {
        DATASOURCE_CONTEXT.set(SLAVE);
    }

    public static void clearDataSourceContext() {
        DATASOURCE_CONTEXT.remove();
    }

    public static String getCurrentDataSourceContext() {
        return DATASOURCE_CONTEXT.get();
    }

}
