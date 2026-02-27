package com.klzw.common.database.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源类
 * 用于管理主从数据源的切换
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 数据源上下文，用于存储当前线程使用的数据源类型
     */
    private static final ThreadLocal<String> DATASOURCE_CONTEXT = new ThreadLocal<>();

    /**
     * 主数据源类型
     */
    public static final String MASTER = "master";

    /**
     * 从数据源类型
     */
    public static final String SLAVE = "slave";

    /**
     * 确定当前线程使用的数据源类型
     * @return 数据源类型
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DATASOURCE_CONTEXT.get();
    }

    /**
     * 设置使用主数据源
     */
    public static void setMasterDataSource() {
        DATASOURCE_CONTEXT.set(MASTER);
    }

    /**
     * 设置使用从数据源
     */
    public static void setSlaveDataSource() {
        DATASOURCE_CONTEXT.set(SLAVE);
    }

    /**
     * 清除数据源上下文
     */
    public static void clearDataSourceContext() {
        DATASOURCE_CONTEXT.remove();
    }

}
