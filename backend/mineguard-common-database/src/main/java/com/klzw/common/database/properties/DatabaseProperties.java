package com.klzw.common.database.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.database")
public class DatabaseProperties {

    private boolean enabled = true;

    private final MybatisPlus mybatisPlus = new MybatisPlus();

    private final Druid druid = new Druid();

    private final Dynamic dynamic = new Dynamic();

    private final Datasource datasource = new Datasource();

    @Data
    public static class MybatisPlus {
        private boolean overflow = true;
        private String mapperLocations = "classpath*:mapper/**/*.xml";
        private String typeAliasesPackage = "com.klzw.**.domain";
    }

    @Data
    public static class Druid {
        private int initialSize = 3;
        private int minIdle = 3;
        private int maxActive = 10;
        private long maxWait = 60000L;
        private long timeBetweenEvictionRunsMillis = 60000L;
        private long minEvictableIdleTimeMillis = 300000L;
        private String validationQuery = "SELECT 1 FROM DUAL";
        private boolean testWhileIdle = true;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean poolPreparedStatements = true;
        private int maxPoolPreparedStatementPerConnectionSize = 20;
        private String filters = "stat,wall,slf4j";
        private String connectionProperties = "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000";
        private String statViewUsername = "admin";
        private String statViewPassword = "";
        private String statViewAllow = "";
    }

    @Data
    public static class Dynamic {
        private boolean enabled = false;
        private String primary = "master";
        private String[] slaves = new String[]{};
    }

    @Data
    public static class Datasource {
        private final Master master = new Master();
        private final Slave slave = new Slave();

        @Data
        public static class Master {
            private String url;
            private String username;
            private String password;
            private String driverClassName;
        }

        @Data
        public static class Slave {
            private String url;
            private String username;
            private String password;
            private String driverClassName;
        }
    }
}
