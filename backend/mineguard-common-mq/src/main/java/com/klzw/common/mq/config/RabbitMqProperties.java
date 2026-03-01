package com.klzw.common.mq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 配置属性类
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
@Setter
public class RabbitMqProperties {

    /**
     * RabbitMQ 主机地址
     */
    private String host = "localhost";

    /**
     * RabbitMQ 端口
     */
    private int port = 5672;

    /**
     * RabbitMQ 用户名
     */
    private String username = "guest";

    /**
     * RabbitMQ 密码
     */
    private String password = "guest";

    /**
     * RabbitMQ 虚拟主机
     */
    private String virtualHost = "/";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 60000;

    /**
     * 通道缓存大小
     */
    private int channelCacheSize = 25;

}
