package com.klzw.common.mongodb.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MongoDB 配置属性类
 * 用于读取应用配置中的 MongoDB 相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mongodb")
public class MongoDbProperties {

    /**
     * MongoDB 连接 URI
     */
    private String uri;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private int port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 认证数据库
     */
    private String authenticationDatabase;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;

    /**
     * 最大连接数
     */
    private int maxConnections = 100;

    /**
     * 最小连接数
     */
    private int minConnectionsPerHost = 10;

    /**
     * 连接池最大等待时间（毫秒）
     */
    private int maxWaitTime = 120000;

    /**
     * 心跳间隔时间（毫秒）
     */
    private int heartbeatFrequency = 20000;

    /**
     * 心跳超时时间（毫秒）
     */
    private int heartbeatTimeout = 10000;

    /**
     * 获取连接字符串
     * 如果配置了 uri 则直接返回，否则根据 host、port、username、password 构建
     * @return 连接字符串
     */
    public String getConnectionString() {
        if (uri != null && !uri.isEmpty()) {
            return uri;
        }
        
        StringBuilder sb = new StringBuilder("mongodb://");
        
        if (username != null && !username.isEmpty()) {
            sb.append(username);
            if (password != null && !password.isEmpty()) {
                sb.append(":").append(password);
            }
            sb.append("@");
        }
        
        sb.append(host != null ? host : "localhost");
        sb.append(":").append(port > 0 ? port : 27017);
        sb.append("/").append(database != null ? database : "test");
        
        if (authenticationDatabase != null && !authenticationDatabase.isEmpty()) {
            sb.append("?authSource=").append(authenticationDatabase);
        }
        
        return sb.toString();
    }

}
