package com.klzw.common.database.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库配置属性
 * 通过 @ConfigurationProperties 暴露可配置项，由服务模块或 Nacos 提供实际值
 * <p>
 * 配置前缀：mineguard.database
 * <p>
 * 主要配置项：
 * 1. MyBatis-Plus 配置：分页插件、Mapper 扫描路径等
 * 2. Druid 连接池配置：连接数、超时时间等
 * 3. 动态数据源配置：主从数据源切换等
 * 4. 数据源连接信息：URL、用户名、密码等
 */
@Data
@ConfigurationProperties(prefix = "mineguard.database")
public class DatabaseProperties {

    /**
     * MyBatis-Plus 配置
     */
    private final MybatisPlus mybatisPlus = new MybatisPlus();

    /**
     * Druid 连接池配置
     */
    private final Druid druid = new Druid();

    /**
     * 动态数据源配置
     */
    private final Dynamic dynamic = new Dynamic();

    /**
     * 数据源连接信息
     */
    private final Datasource datasource = new Datasource();

    /**
     * MyBatis-Plus 配置
     * <p>
     * 包含分页插件、Mapper 扫描路径等配置
     * 注：分页参数（默认页码、页大小、最大值）已移至 common-core 的 PaginationProperties
     */
    @Data
    public static class MybatisPlus {
        /**
         * 分页插件是否溢出处理
         * true: 当页码超过总页数时，会查询最后一页
         * false: 当页码超过总页数时，会返回空结果
         */
        private boolean overflow = true;

        /**
         * Mapper 扫描路径
         * 默认扫描所有模块下的 mapper 目录
         */
        private String mapperLocations = "classpath*:mapper/**/*.xml";

        /**
         * 类型别名包路径
         * 默认扫描所有模块下的 domain 包
         */
        private String typeAliasesPackage = "com.klzw.**.domain";
    }

    /**
     * Druid 连接池配置
     * <p>
     * 包含连接数、超时时间、验证查询等配置
     */
    @Data
    public static class Druid {
        /**
         * 初始连接数
         */
        private int initialSize = 5;

        /**
         * 最小空闲连接数
         */
        private int minIdle = 5;

        /**
         * 最大连接数
         */
        private int maxActive = 20;

        /**
         * 获取连接超时时间（毫秒）
         */
        private long maxWait = 60000L;

        /**
         * 连接有效性检查时间间隔（毫秒）
         */
        private long timeBetweenEvictionRunsMillis = 60000L;

        /**
         * 连接最大空闲时间（毫秒）
         */
        private long minEvictableIdleTimeMillis = 300000L;

        /**
         * 连接有效性检查 SQL
         */
        private String validationQuery = "SELECT 1 FROM DUAL";

        /**
         * 是否在空闲时检查连接有效性
         */
        private boolean testWhileIdle = true;

        /**
         * 是否在获取连接时检查有效性
         */
        private boolean testOnBorrow = false;

        /**
         * 是否在归还连接时检查有效性
         */
        private boolean testOnReturn = false;

        /**
         * 是否开启池化连接
         */
        private boolean poolPreparedStatements = true;

        /**
         * 池化连接最大数量
         */
        private int maxPoolPreparedStatementPerConnectionSize = 20;

        /**
         * 过滤器
         * stat: 监控统计
         * wall: 防火墙
         * log4j: 日志
         */
        private String filters = "stat,wall,log4j";

        /**
         * 连接属性
         */
        private String connectionProperties = "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000";

        /**
         * 监控页面访问账号
         */
        private String statViewUsername = "admin";

        /**
         * 监控页面访问密码
         */
        private String statViewPassword = "";

        /**
         * 监控页面访问允许IP
         */
        private String statViewAllow = "";
    }

    /**
     * 动态数据源配置
     * <p>
     * 包含主从数据源切换等配置
     */
    @Data
    public static class Dynamic {
        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 主数据源名称
         */
        private String primary = "master";

        /**
         * 从数据源名称列表
         */
        private String[] slaves = new String[]{};
    }

    /**
     * 数据源连接信息
     * <p>
     * 包含主从数据源的连接信息
     */
    @Data
    public static class Datasource {
        /**
         * 主数据源配置
         */
        private final Master master = new Master();

        /**
         * 从数据源配置
         */
        private final Slave slave = new Slave();

        /**
         * 主数据源配置
         */
        @Data
        public static class Master {
            /**
             * 数据库URL
             */
            private String url;

            /**
             * 数据库用户名
             */
            private String username;

            /**
             * 数据库密码
             */
            private String password;

            /**
             * 数据库驱动类名
             */
            private String driverClassName;
        }

        /**
         * 从数据源配置
         */
        @Data
        public static class Slave {
            /**
             * 数据库URL
             */
            private String url;

            /**
             * 数据库用户名
             */
            private String username;

            /**
             * 数据库密码
             */
            private String password;

            /**
             * 数据库驱动类名
             */
            private String driverClassName;
        }
    }
}
