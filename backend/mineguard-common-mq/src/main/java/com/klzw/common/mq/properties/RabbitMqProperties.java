package com.klzw.common.mq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.mq.rabbit")
public class RabbitMqProperties {

    private boolean enabled = true;

    private String host = "localhost";

    private Integer port = 5672;

    private String username = "guest";

    private String password = "guest";

    private String virtualHost = "/";

    private Integer connectionTimeout = 60000;

    private Integer channelCacheSize = 25;
}
