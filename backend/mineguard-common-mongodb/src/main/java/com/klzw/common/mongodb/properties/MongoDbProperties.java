package com.klzw.common.mongodb.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.mongodb")
public class MongoDbProperties {

    private boolean enabled = true;

    private String host = "localhost";

    private Integer port = 27017;

    private String database;

    private String username;

    private String password;

    private String authenticationDatabase = "admin";

    private Integer connectTimeout = 10000;

    private Integer readTimeout = 10000;

    private Integer maxConnections = 100;

    private Integer minConnectionsPerHost = 10;

    private Integer maxWaitTime = 120000;

    private Integer heartbeatFrequency = 10000;

    public String getConnectionString() {
        StringBuilder sb = new StringBuilder("mongodb://");
        
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            sb.append(username).append(":").append(password).append("@");
        }
        
        sb.append(host).append(":").append(port);
        
        if (database != null && !database.isEmpty()) {
            sb.append("/").append(database);
        }
        
        if (username != null && !username.isEmpty()) {
            sb.append("?authSource=").append(authenticationDatabase);
        }
        
        return sb.toString();
    }
}
